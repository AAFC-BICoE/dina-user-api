package ca.gc.aafc.dinauser.api;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.repository.UserRepository;
import ca.gc.aafc.dinauser.api.service.DinaUserService;
import ca.gc.aafc.dinauser.api.service.KeycloakClientService;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.admin.client.KeycloakBuilder;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SpringBootTest(classes = DinaUserModuleApiLauncher.class)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
public class UserRepoTest {

  @Container
  private static final DinaKeycloakTestContainer keycloak = DinaKeycloakTestContainer.getInstance();
  public static final QuerySpec QUERY_SPEC = new QuerySpec(DinaUserDto.class);

  @Inject
  private UserRepository userRepository;
  @Inject
  private DinaUserService service;

  @MockBean
  KeycloakClientService keycloakClientService;

  private DinaUserDto persisted;

  @BeforeAll
  static void beforeAll() {
    keycloak.start();
  }

  @BeforeEach
  void setUp() {
    mockKeycloakClienService();
    persisted = service.create(newUserDto());
  }

  @AfterEach
  void tearDown() {
    service.deleteUser(persisted.getInternalId());
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc/COLLECTION_MANAGER", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void findOne_WhenCollectionManager_ReturnsRecord() {
    DinaUserDto result = userRepository.findOne(persisted.getInternalId(), QUERY_SPEC);
    Assertions.assertEquals(persisted.getAgentId(), result.getAgentId());
    Assertions.assertEquals(persisted.getUsername(), result.getUsername());
    Assertions.assertEquals(persisted.getFirstName(), result.getFirstName());
    Assertions.assertEquals(persisted.getLastName(), result.getLastName());
    Assertions.assertEquals(persisted.getEmailAddress(), result.getEmailAddress());
    Assertions.assertEquals(persisted.getRolesPerGroup().get("cnc"), result.getRolesPerGroup().get("cnc"));
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc/student", internalIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void findOne_WhenStudentRequestsOtherRecord_ThrowsForbidden() {
    Assertions.assertThrows(ForbiddenException.class, () -> userRepository.findOne(
      persisted.getInternalId(), QUERY_SPEC));
  }

  @Test
  void findOne_StudentRequestSelf_StudentRecordReturned() {
    mockAuthenticatedUserWithPersisted("dao/student");
    DinaUserDto result = userRepository.findOne(persisted.getInternalId(), QUERY_SPEC);
    Assertions.assertEquals(persisted.getInternalId(), result.getInternalId());
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc/staff", internalIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void findOne_WhenStaffRequestsOtherRecord_ThrowsForbidden() {
    Assertions.assertThrows(ForbiddenException.class, () -> userRepository.findOne(
      persisted.getInternalId(), QUERY_SPEC));
  }

  @Test
  void findOne_StaffRequestSelf_StaffRecordReturned() {
    mockAuthenticatedUserWithPersisted("dao/staff");
    DinaUserDto result = userRepository.findOne(persisted.getInternalId(), QUERY_SPEC);
    Assertions.assertEquals(persisted.getInternalId(), result.getInternalId());
  }

  @Test
  void findAll_WhenStudent_UserRecordReturned() {
    mockAuthenticatedUserWithPersisted("dao/student");
    ResourceList<DinaUserDto> results = userRepository.findAll(QUERY_SPEC);
    Assertions.assertEquals(1, results.size());
    Assertions.assertEquals(persisted.getInternalId(), results.get(0).getInternalId());
  }

  @Test
  void findAll_WhenStaff_UserRecordReturned() {
    mockAuthenticatedUserWithPersisted("dao/staff");
    ResourceList<DinaUserDto> results = userRepository.findAll(QUERY_SPEC);
    Assertions.assertEquals(1, results.size());
    Assertions.assertEquals(persisted.getInternalId(), results.get(0).getInternalId());
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc/COLLECTION_MANAGER", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void findAll_WhenManager_AllRecordsReturned() {
    ResourceList<DinaUserDto> results = userRepository.findAll(QUERY_SPEC);
    Assertions.assertEquals(7, results.size());
    MatcherAssert.assertThat(
      results.stream().map(DinaUserDto::getUsername).collect(Collectors.toSet()),
      Matchers.hasItems(
        "cnc-cm",
        "admin",
        "user",
        "cnc-staff",
        "dina-admin",
        persisted.getUsername()));
    DinaUserDto resultDto = results.stream()
      .filter(dinaUserDto -> dinaUserDto.getInternalId().equalsIgnoreCase(persisted.getInternalId()))
      .findFirst()
      .orElseGet(() -> Assertions.fail("persisted user not returned"));
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc/DINA_ADMIN", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
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
  @WithMockKeycloakUser(groupRole = "cnc/COLLECTION_MANAGER", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void create_WhenInvalidRole_ThrowsForbidden() {
    Assertions.assertThrows(ForbiddenException.class, () -> userRepository.create(newUserDto()));
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc/DINA_ADMIN", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
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
  @WithMockKeycloakUser(groupRole = "cnc/COLLECTION_MANAGER", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void update_WhenInvalidRole_ThrowsForbidden() {
    Assertions.assertThrows(ForbiddenException.class, () -> userRepository.save(persisted));
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc/COLLECTION_MANAGER", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void update_WhenUpdatingBeyondCurrentUsersRole_ThrowsForbidden() {
    // Add new student
    DinaUserDto newUser = newUserDto();
    newUser.setRolesPerGroup(Map.of("cnc", Set.of("student")));
    String id = userRepository.create(newUser).getInternalId();
    // Try to update student to collection manager
    DinaUserDto toUpdate = userRepository.findOne(id, QUERY_SPEC);
    toUpdate.setRolesPerGroup(Map.of("cnc", Set.of("collection-manager")));
    Assertions.assertThrows(ForbiddenException.class, () -> userRepository.save(toUpdate));
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc/DINA_ADMIN", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
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
  @WithMockKeycloakUser(groupRole = "cnc/staff", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void delete_WhenInvalidRole_ThrowsForbidden() {
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
      .rolesPerGroup(Map.of("cnc", Set.of("collection-manager")))
      .build();
  }

  private void mockKeycloakClienService() {
    Mockito.when(keycloakClientService.getKeycloakClient()).thenReturn(
      KeycloakBuilder.builder()
        .serverUrl("http://localhost:" + keycloak.getHttpPort() + "/auth")
        .realm("dina")
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .clientId("user-svc")
        .clientSecret("120c0b7a-5ed2-4295-9a31-29c2fcbc714f").build());
    Mockito.when(keycloakClientService.getRealm()).thenReturn("dina");
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
