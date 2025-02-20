package ca.gc.aafc.dinauser.api.repository;

import ca.gc.aafc.dina.security.DinaRole;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.dinauser.api.DinaKeycloakTestContainer;
import ca.gc.aafc.dinauser.api.DinaUserModuleApiLauncher;
import ca.gc.aafc.dinauser.api.UserModuleTestConfiguration;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.service.DinaUserService;
import ca.gc.aafc.dinauser.api.service.KeycloakClientService;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SpringBootTest(classes = {UserModuleTestConfiguration.class, DinaUserModuleApiLauncher.class})
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
public class UserRepoTest {

  @Container
  private static final DinaKeycloakTestContainer keycloak = DinaKeycloakTestContainer.getInstance();
  private static final String TEST_REALM = "dina";
  private static final String KEYCLOAK_INTERNAL_USER_ROLE = "dina-realm-user";

  public static final QuerySpec QUERY_SPEC = new QuerySpec(DinaUserDto.class);

  @Inject
  private UserRepository userRepository;

  @Inject
  private DinaUserService service;

  @MockBean
  private KeycloakClientService keycloakClientService;

  @Inject
  private Keycloak keycloakClient;

  private DinaUserDto persisted;

  @BeforeAll
  static void beforeAll() {
    keycloak.start();
  }

  @BeforeEach
  void setUp() {
    mockKeycloakClientService();
    persisted = service.create(newUserDto());
  }

  @AfterEach
  void tearDown() {
    service.deleteUser(persisted.getInternalId());
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc:SUPER_USER", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void findOne_WhenSuperUser_ReturnsRecord() {
    DinaUserDto result = userRepository.findOne(persisted.getInternalId(), QUERY_SPEC);
    Assertions.assertEquals(persisted.getAgentId(), result.getAgentId());
    Assertions.assertEquals(persisted.getUsername(), result.getUsername());
    Assertions.assertEquals(persisted.getFirstName(), result.getFirstName());
    Assertions.assertEquals(persisted.getLastName(), result.getLastName());
    Assertions.assertEquals(persisted.getEmailAddress(), result.getEmailAddress());
    Assertions.assertEquals(persisted.getRolesPerGroup().get("cnc"), result.getRolesPerGroup().get("cnc"));
    Assertions.assertEquals(persisted.getAdminRoles().size(), 0);
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc:GUEST", internalIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void findOne_WhenGuestRequestsOtherRecord_ThrowsForbidden() {
    Assertions.assertThrows(ForbiddenException.class, () -> userRepository.findOne(
      persisted.getInternalId(), QUERY_SPEC));
  }

  @Test
  void findOne_WhenGuestRequestSelf_RecordReturned() {
    mockAuthenticatedUserWithPersisted("dao/guest");
    DinaUserDto result = userRepository.findOne(persisted.getInternalId(), QUERY_SPEC);
    Assertions.assertEquals(persisted.getInternalId(), result.getInternalId());
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc:user", internalIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void findOne_WhenUserRequestsOtherRecord_ThrowsForbidden() {
    Assertions.assertThrows(ForbiddenException.class, () -> userRepository.findOne(
      persisted.getInternalId(), QUERY_SPEC));
  }

  @Test
  void findOne_UserRequestSelf_UserRecordReturned() {
    mockAuthenticatedUserWithPersisted("dao/user");
    DinaUserDto result = userRepository.findOne(persisted.getInternalId(), QUERY_SPEC);
    Assertions.assertEquals(persisted.getInternalId(), result.getInternalId());
  }

  @Test
  void findAll_WhenGuest_UserRecordReturned() {
    mockAuthenticatedUserWithPersisted("dao/guest");
    ResourceList<DinaUserDto> results = userRepository.findAll(QUERY_SPEC);
    Assertions.assertEquals(1, results.size());
    Assertions.assertEquals(persisted.getInternalId(), results.get(0).getInternalId());
  }

  @Test
  void findAll_WhenUser_UserRecordReturned() {
    mockAuthenticatedUserWithPersisted("dao/user");
    ResourceList<DinaUserDto> results = userRepository.findAll(QUERY_SPEC);
    Assertions.assertEquals(1, results.size());
    Assertions.assertEquals(persisted.getInternalId(), results.get(0).getInternalId());
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc:SUPER_USER", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void findAll_WhenSuperUser_AllRecordsReturned() {
    ResourceList<DinaUserDto> results = userRepository.findAll(QUERY_SPEC);
    Assertions.assertEquals(5, results.size());
    MatcherAssert.assertThat(
      results.stream().map(DinaUserDto::getUsername).collect(Collectors.toSet()),
      Matchers.hasItems(
        "cnc-su",
        "cnc-user",
        "cnc-guest",
        "cnc-ro",
        persisted.getUsername()));
    DinaUserDto resultDto = results.stream()
      .filter(dinaUserDto -> dinaUserDto.getInternalId().equalsIgnoreCase(persisted.getInternalId()))
      .findFirst()
      .orElseGet(() -> Assertions.fail("persisted user not returned"));
    Assertions.assertEquals(persisted.getRolesPerGroup().get("cnc"), resultDto.getRolesPerGroup().get("cnc"));
  }

  @Test
  @WithMockKeycloakUser(adminRole = "DINA_ADMIN", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void create_RecordCreated() {
    DinaUserDto expected = newUserDto();
    DinaUserDto result = userRepository.findOne(
      userRepository.create(expected).getInternalId(),
      QUERY_SPEC);
    Assertions.assertEquals(expected.getAgentId(), result.getAgentId());
    Assertions.assertEquals(expected.getUsername(), result.getUsername());
    Assertions.assertEquals(expected.getFirstName(), result.getFirstName());
    Assertions.assertEquals(expected.getLastName(), result.getLastName());
    Assertions.assertEquals(expected.getEmailAddress(), result.getEmailAddress());
    Assertions.assertEquals(expected.getRolesPerGroup().get("cnc"), result.getRolesPerGroup().get("cnc"));
    userRepository.delete(result.getInternalId());
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc:SUPER_USER", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void create_WhenCreateUserWithSameRole_ThrowsForbidden() {
    Assertions.assertThrows(ForbiddenException.class, () -> userRepository.create(newUserDto()));
  }

  @Test
  @WithMockKeycloakUser(adminRole = "DINA_ADMIN", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void update_RecordUpdated() {
    DinaUserDto update = userRepository.findOne(persisted.getInternalId(), QUERY_SPEC);

    String expected_first_name = "expected first name";
    String expected_last_name = "expected last name";
    String expected_email = "expected@email.com";

    update.setFirstName(expected_first_name);
    update.setEmailAddress(expected_email);
    update.setLastName(expected_last_name);
    userRepository.save(update);

    DinaUserDto result = userRepository.findOne(persisted.getInternalId(), QUERY_SPEC);

    Assertions.assertEquals(expected_first_name, result.getFirstName());
    Assertions.assertEquals(expected_last_name, result.getLastName());
    Assertions.assertEquals(expected_email, result.getEmailAddress());
  }

  @Test
  @WithMockKeycloakUser(adminRole = "DINA_ADMIN", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void update_WhenGroupsRemoved_GroupsRemovedSuccessfully() {
    DinaUserDto update = userRepository.findOne(persisted.getInternalId(), QUERY_SPEC);

    Assertions.assertEquals(1, update.getRolesPerGroup().size());
    update.setRolesPerGroup(Map.of());
    userRepository.save(update);

    DinaUserDto result = userRepository.findOne(persisted.getInternalId(), QUERY_SPEC);
    Assertions.assertEquals(0, result.getRolesPerGroup().size());

    // Make sure Keycloak internal role is still there
    RoleScopeResource rsr = keycloakClient.realm(TEST_REALM).users().get(persisted.getInternalId()).roles().realmLevel();
    Optional<?> userRole = rsr.listEffective().stream()
        .filter(r -> KEYCLOAK_INTERNAL_USER_ROLE.equals(r.getName()))
        .findFirst();
    Assertions.assertFalse(userRole.isEmpty());
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc:SUPER_USER", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void update_WhenUpdateWithSameRole_ThrowsForbidden() {
    Assertions.assertThrows(ForbiddenException.class, () -> userRepository.save(persisted));
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc:SUPER_USER", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void update_WhenUpdatingBeyondCurrentUsersRole_ThrowsForbidden() {
    // Add new student
    DinaUserDto newUser = newUserDto();
    newUser.setRolesPerGroup(Map.of("cnc", Set.of(DinaRole.GUEST.getKeycloakRoleName())));
    String id = userRepository.create(newUser).getInternalId();
    // Try to update student to collection manager
    DinaUserDto toUpdate = userRepository.findOne(id, QUERY_SPEC);
    toUpdate.setRolesPerGroup(Map.of("cnc", Set.of(DinaRole.SUPER_USER.getKeycloakRoleName())));
    Assertions.assertThrows(ForbiddenException.class, () -> userRepository.save(toUpdate));
  }

  @Test
  @WithMockKeycloakUser(adminRole = "DINA_ADMIN", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void delete_RecordDeleted() {
    DinaUserDto newUser = userRepository.create(newUserDto());

    DinaUserDto result = userRepository.findOne(newUser.getInternalId(), QUERY_SPEC);
    Assertions.assertNotNull(result);

    userRepository.delete(result.getInternalId());
    Assertions.assertThrows(
      NotFoundException.class,
      () -> userRepository.findOne(newUser.getInternalId(), QUERY_SPEC));
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc:user", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void delete_WhenTryToDeleteUserWithHigherRole_ThrowsForbidden() {
    Assertions.assertThrows(
      ForbiddenException.class,
      () -> userRepository.delete(persisted.getInternalId()));
  }

  private DinaUserDto newUserDto() {
    return DinaUserDto.builder()
      .agentId(UUID.randomUUID().toString())
      .username(RandomStringUtils.randomAlphabetic(5).toLowerCase())
      .firstName(RandomStringUtils.randomAlphabetic(5).toLowerCase())
      .lastName(RandomStringUtils.randomAlphabetic(5).toLowerCase())
      .emailAddress(RandomStringUtils.randomAlphabetic(5).toLowerCase() + "@user.com")
      .rolesPerGroup(Map.of("cnc", Set.of(DinaRole.SUPER_USER.getKeycloakRoleName())))
      .build();
  }

  private void mockKeycloakClientService() {
    Mockito.when(keycloakClientService.getKeycloakClient()).thenReturn(keycloakClient);
    Mockito.when(keycloakClientService.getRealm()).thenReturn(TEST_REALM);
  }

  private void mockAuthenticatedUserWithPersisted(String group) {
    KeycloakAuthenticationToken mockToken = Mockito.mock(
      KeycloakAuthenticationToken.class,
      Answers.RETURNS_DEEP_STUBS
    );
    Mockito.when(mockToken.getName()).thenReturn("test-user");
    Mockito.when(mockToken.getAccount().getKeycloakSecurityContext().getToken().getSubject())
      .thenReturn(persisted.getInternalId());
    mockClaim(mockToken, "groups", List.of(group));
    SecurityContextHolder.getContext().setAuthentication(mockToken);
  }

  public static void mockClaim(KeycloakAuthenticationToken token, String key, Object value) {
    Mockito.when(token.getAccount().getKeycloakSecurityContext().getToken().getOtherClaims()
      .get(key)).thenReturn(value);
    Mockito.when(token.getAccount().getKeycloakSecurityContext().getToken().getOtherClaims()
      .getOrDefault(key, "")).thenReturn(value);
  }

}
