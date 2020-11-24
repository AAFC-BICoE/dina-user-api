package ca.gc.aafc.dinauser.api;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.repository.UserRepository;
import ca.gc.aafc.dinauser.api.service.KeycloakClientService;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@SpringBootTest(classes = DinaUserModuleApiLauncher.class)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
public class UserRepoTest {

  @Container
  private static final DinaKeycloakTestContainer keycloak = DinaKeycloakTestContainer.getInstance();

  @Inject
  private UserRepository userRepository;

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
    persisted = userRepository.create(newUserDto());
  }

  @AfterEach
  void tearDown() {
    userRepository.delete(persisted.getInternalId());
  }

  @Test
  void findAll_ReturnsAllRecords() {
    ResourceList<DinaUserDto> results = userRepository.findAll(new QuerySpec(DinaUserDto.class));
    Assertions.assertEquals(5, results.size());
    MatcherAssert.assertThat(
      results.stream().map(DinaUserDto::getUsername).collect(Collectors.toSet()),
      Matchers.containsInAnyOrder("cnc-cm", "admin", "user", "cnc-staff", persisted.getUsername()));
  }

  @Test
  void create_RecordCreated() {
    DinaUserDto expected = newUserDto();
    DinaUserDto result = userRepository.findOne(
      userRepository.create(expected).getInternalId(),
      new QuerySpec(DinaUserDto.class));
    Assertions.assertEquals(expected.getAgentId(), result.getAgentId());
    Assertions.assertEquals(expected.getUsername(), result.getUsername());
    Assertions.assertEquals(expected.getFirstName(), result.getFirstName());
    Assertions.assertEquals(expected.getLastName(), result.getLastName());
    Assertions.assertEquals(expected.getEmailAddress(), result.getEmailAddress());
    MatcherAssert.assertThat(
      result.getRoles(),
      Matchers.containsInAnyOrder("collection-manager"));
    MatcherAssert.assertThat(
      result.getGroups(),
      Matchers.containsInAnyOrder("/cnc/collection-manager"));
    userRepository.delete(result.getInternalId());
  }

  @Test
  void update_RecordUpdated() {
    DinaUserDto update = userRepository.findOne(
      persisted.getInternalId(),
      new QuerySpec(DinaUserDto.class));

    String expected_first_name = "expected first name";
    String expected_last_name = "expected last name";
    String expected_email = "expected@email.com";

    update.setFirstName(expected_first_name);
    update.setEmailAddress(expected_email);
    update.setLastName(expected_last_name);
    userRepository.save(update);

    DinaUserDto result = userRepository.findOne(
      persisted.getInternalId(),
      new QuerySpec(DinaUserDto.class));

    Assertions.assertEquals(expected_first_name, result.getFirstName());
    Assertions.assertEquals(expected_last_name, result.getLastName());
    Assertions.assertEquals(expected_email, result.getEmailAddress());
  }

  @Test
  void delete_RecordDeleted() {
    DinaUserDto newUser = userRepository.create(newUserDto());

    DinaUserDto result = userRepository.findOne(
      newUser.getInternalId(),
      new QuerySpec(DinaUserDto.class));
    Assertions.assertNotNull(result);

    userRepository.delete(result.getInternalId());
    Assertions.assertThrows(
      NotFoundException.class,
      () -> userRepository.findOne(newUser.getInternalId(), new QuerySpec(DinaUserDto.class)));
  }

  private DinaUserDto newUserDto() {
    return DinaUserDto.builder()
      .agentId(UUID.randomUUID().toString())
      .username(RandomStringUtils.randomAlphabetic(5).toLowerCase())
      .firstName(RandomStringUtils.randomAlphabetic(5).toLowerCase())
      .lastName(RandomStringUtils.randomAlphabetic(5).toLowerCase())
      .emailAddress(RandomStringUtils.randomAlphabetic(5).toLowerCase() + "@user.com")
      .groups(List.of("/cnc/collection-manager"))
      .roles(List.of("collection-manager"))
      .build();
  }

  private void mockKeycloakClienService() {
    Mockito.when(keycloakClientService.getKeycloakClient()).thenReturn(
      Keycloak.getInstance(
        "http://localhost:" + keycloak.getHttpPort() + "/auth",
        "dina",
        "admin",
        "admin",
        "admin-cli"));
    Mockito.when(keycloakClientService.getRealm()).thenReturn("dina");
  }
}
