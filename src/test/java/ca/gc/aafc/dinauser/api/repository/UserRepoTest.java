package ca.gc.aafc.dinauser.api.repository;

import ca.gc.aafc.dina.dto.JsonApiDto;
import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.filter.QueryComponent;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.jsonapi.JsonApiDocuments;
import ca.gc.aafc.dina.repository.DinaRepositoryV2;
import ca.gc.aafc.dina.security.DinaRole;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.dinauser.api.DinaKeycloakTestContainer;
import ca.gc.aafc.dinauser.api.DinaUserModuleApiLauncher;
import ca.gc.aafc.dinauser.api.UserModuleTestKeycloakConfiguration;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.service.DinaUserService;
import ca.gc.aafc.dinauser.api.service.KeycloakClientService;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {UserModuleTestKeycloakConfiguration.class, DinaUserModuleApiLauncher.class})
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
public class UserRepoTest {

  @Container
  private static final DinaKeycloakTestContainer keycloak = DinaKeycloakTestContainer.getInstance();
  private static final String TEST_REALM = "dina";
  private static final String KEYCLOAK_INTERNAL_USER_ROLE = "dina-realm-user";

  @Inject
  private UserRepository userRepository;

  @Inject
  private DinaUserService service;

  @MockBean
  private KeycloakClientService keycloakClientService;

  @Inject
  private Keycloak keycloakClient;

  private DinaUserDto persisted;
  private UUID persistedUUID;

  @BeforeAll
  static void beforeAll() {
    keycloak.start();
  }

  @BeforeEach
  void setUp() {
    mockKeycloakClientService();
    persisted = service.create(newUserDto());
    persistedUUID = persisted.getJsonApiId();
  }

  @AfterEach
  void tearDown() {
    service.deleteUser(persisted.getInternalId());
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc:SUPER_USER", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void findOne_WhenSuperUser_ReturnsRecord() throws ResourceNotFoundException,
    ResourceGoneException {
    DinaUserDto result = userRepository.getOne(persistedUUID).getDto();
    assertEquals(persisted.getAgentId(), result.getAgentId());
    assertEquals(persisted.getUsername(), result.getUsername());
    assertEquals(persisted.getFirstName(), result.getFirstName());
    assertEquals(persisted.getLastName(), result.getLastName());
    assertEquals(persisted.getEmailAddress(), result.getEmailAddress());
    assertEquals(persisted.getRolesPerGroup().get("cnc"), result.getRolesPerGroup().get("cnc"));
    assertEquals(persisted.getAdminRoles().size(), 0);
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc:GUEST", internalIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void findOne_WhenGuestRequestsOtherRecord_ThrowsForbidden() {
    assertThrows(AccessDeniedException.class, () -> userRepository.getOne(
      persistedUUID));
  }

  @Test
  void findOne_WhenGuestRequestSelf_RecordReturned()
    throws ResourceNotFoundException, ResourceGoneException {
    mockAuthenticatedUserWithPersisted("dao/guest");
    DinaUserDto result = userRepository.getOne(persistedUUID).getDto();
    assertEquals(persisted.getInternalId(), result.getInternalId());
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc:user", internalIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void findOne_WhenUserRequestsOtherRecord_ThrowsForbidden() {
    assertThrows(AccessDeniedException.class, () -> userRepository.onFindOne(persistedUUID));
  }

  @Test
  void findOne_UserRequestSelf_UserRecordReturned()
    throws ResourceNotFoundException, ResourceGoneException {
    mockAuthenticatedUserWithPersisted("dao/user");
    DinaUserDto result = userRepository.getOne(persistedUUID).getDto();
    assertEquals(persisted.getInternalId(), result.getInternalId());
  }

  @Test
  void findAll_WhenGuest_UserRecordReturned() {
    mockAuthenticatedUserWithPersisted("dao/guest");
    DinaRepositoryV2.PagedResource<JsonApiDto<DinaUserDto>> results = userRepository.getAll(QueryComponent.EMPTY);
    assertEquals(1, results.totalCount());
    assertEquals(persisted.getInternalId(), results.resourceList().getFirst().getDto().getInternalId());
  }

  @Test
  void findAll_WhenUser_UserRecordReturned() {
    mockAuthenticatedUserWithPersisted("dao/user");
    DinaRepositoryV2.PagedResource<JsonApiDto<DinaUserDto>> results = userRepository.getAll(QueryComponent.EMPTY);
    assertEquals(1, results.totalCount());
    assertEquals(persisted.getInternalId(), results.resourceList().getFirst().getDto().getInternalId());
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc:SUPER_USER", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void findAll_WhenSuperUser_AllRecordsReturned() {
    DinaRepositoryV2.PagedResource<JsonApiDto<DinaUserDto>> results = userRepository.getAll(QueryComponent.EMPTY);
    assertEquals(5, results.totalCount());
    MatcherAssert.assertThat(
      results.resourceList().stream().map( d -> d.getDto().getUsername()).collect(Collectors.toSet()),
      Matchers.hasItems(
        "cnc-su",
        "cnc-user",
        "cnc-guest",
        "cnc-ro",
        persisted.getUsername()));
    DinaUserDto resultDto = results.resourceList().stream()
      .filter(d -> d.getDto().getInternalId().equalsIgnoreCase(persisted.getInternalId()))
      .findFirst()
      .map(JsonApiDto::getDto)
      .orElseGet(() -> Assertions.fail("persisted user not returned"));
    assertEquals(persisted.getRolesPerGroup().get("cnc"), resultDto.getRolesPerGroup().get("cnc"));
  }

  @Test
  @WithMockKeycloakUser(adminRole = "DINA_ADMIN", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void create_RecordCreated() throws ResourceNotFoundException, ResourceGoneException {
    DinaUserDto expected = newUserDto();
    JsonApiDocument docToCreate = JsonApiDocuments.createJsonApiDocument(
      UUID.randomUUID(), DinaUserDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(expected));

    DinaUserDto result = userRepository.getOne(
      userRepository.create(docToCreate, null).getDto().getJsonApiId()).getDto();
    assertEquals(expected.getAgentId(), result.getAgentId());
    assertEquals(expected.getUsername(), result.getUsername());
    assertEquals(expected.getFirstName(), result.getFirstName());
    assertEquals(expected.getLastName(), result.getLastName());
    assertEquals(expected.getEmailAddress(), result.getEmailAddress());
    assertEquals(expected.getRolesPerGroup().get("cnc"), result.getRolesPerGroup().get("cnc"));
    userRepository.delete(result.getJsonApiId());
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc:SUPER_USER", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void create_WhenCreateUserWithSameRole_ThrowsForbidden() {
    DinaUserDto expected = newUserDto();

    JsonApiDocument docToCreate = JsonApiDocuments.createJsonApiDocument(
      UUID.randomUUID(), DinaUserDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(expected));

    assertThrows(AccessDeniedException.class, () -> userRepository.create(docToCreate, null));
  }

  @Test
  @WithMockKeycloakUser(adminRole = "DINA_ADMIN", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void update_RecordUpdated() throws ResourceNotFoundException, ResourceGoneException {
    DinaUserDto update = userRepository.getOne(persistedUUID).getDto();

    String expected_first_name = "expected first name";
    String expected_last_name = "expected last name";
    String expected_email = "expected@email.com";

    update.setFirstName(expected_first_name);
    update.setEmailAddress(expected_email);
    update.setLastName(expected_last_name);

    JsonApiDocument docToUpdate = JsonApiDocuments.createJsonApiDocument(
      update.getJsonApiId(), DinaUserDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(update));
    userRepository.update(docToUpdate);

    DinaUserDto result = userRepository.getOne(persistedUUID).getDto();

    assertEquals(expected_first_name, result.getFirstName());
    assertEquals(expected_last_name, result.getLastName());
    assertEquals(expected_email, result.getEmailAddress());
  }

  @Test
  @WithMockKeycloakUser(adminRole = "DINA_ADMIN", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void update_WhenGroupsRemoved_GroupsRemovedSuccessfully()
    throws ResourceNotFoundException, ResourceGoneException {
    DinaUserDto update = userRepository.getOne(persistedUUID).getDto();

    assertEquals(1, update.getRolesPerGroup().size());
    update.setRolesPerGroup(Map.of());

    JsonApiDocument docToUpdate = JsonApiDocuments.createJsonApiDocument(
      update.getJsonApiId(), DinaUserDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(update));

    userRepository.update(docToUpdate);

    DinaUserDto result = userRepository.getOne(persistedUUID).getDto();
    assertEquals(0, result.getRolesPerGroup().size());

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

    JsonApiDocument docToUpdate = JsonApiDocuments.createJsonApiDocument(
      UUID.randomUUID(), DinaUserDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(persisted));

    assertThrows(AccessDeniedException.class, () -> userRepository.update(docToUpdate));
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc:SUPER_USER", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void update_WhenUpdatingBeyondCurrentUsersRole_ThrowsForbidden()
    throws ResourceNotFoundException, ResourceGoneException {
    // Add new student
    DinaUserDto newUser = newUserDto();
    newUser.setRolesPerGroup(Map.of("cnc", Set.of(DinaRole.GUEST.getKeycloakRoleName())));

    JsonApiDocument docToCreate = JsonApiDocuments.createJsonApiDocument(
      UUID.randomUUID(), DinaUserDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(newUser));

    String id = userRepository.create(docToCreate, null).getDto().getInternalId();
    // Try to update student to collection manager
    DinaUserDto toUpdate = userRepository.getOne(UUID.fromString(id)).getDto();
    toUpdate.setRolesPerGroup(Map.of("cnc", Set.of(DinaRole.SUPER_USER.getKeycloakRoleName())));

    JsonApiDocument docToUpdate = JsonApiDocuments.createJsonApiDocument(
      UUID.fromString(id), DinaUserDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(toUpdate));

    assertThrows(AccessDeniedException.class, () -> userRepository.update(docToUpdate));
  }

  @Test
  @WithMockKeycloakUser(adminRole = "DINA_ADMIN", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void delete_RecordDeleted() throws ResourceNotFoundException, ResourceGoneException {

    JsonApiDocument docToCreate = JsonApiDocuments.createJsonApiDocument(
      UUID.randomUUID(), DinaUserDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(newUserDto()));

    DinaUserDto newUser = userRepository.create(docToCreate, null).getDto();

    DinaUserDto result = userRepository.getOne(UUID.fromString(newUser.getInternalId())).getDto();
    assertNotNull(result);

    userRepository.delete(result.getJsonApiId());
    assertThrows(
      ResourceNotFoundException.class,
      () -> userRepository.getOne(UUID.fromString(newUser.getInternalId())));
  }

  @Test
  @WithMockKeycloakUser(groupRole = "cnc:user", agentIdentifier = "34e1de96-cc79-4ce1-8cf6-d0be70ec7bed")
  void delete_WhenTryToDeleteUserWithHigherRole_ThrowsForbidden() {
    assertThrows(
      AccessDeniedException.class, () -> userRepository.delete(persisted.getJsonApiId()));
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
