package ca.gc.aafc.dinauser.api;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.repository.UserRepository;
import ca.gc.aafc.dinauser.api.service.KeycloakClientService;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@SpringBootTest(classes = DinaUserModuleApiLauncher.class)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
public class UserRepoRestIt {

  @Container
  private static final DinaKeycloakTestContainer keycloak = DinaKeycloakTestContainer.getInstance();

  @Inject
  private UserRepository userRepository;

  @MockBean
  KeycloakClientService keycloakClientService;

  @BeforeAll
  static void beforeAll() {
    keycloak.start();
  }

  @BeforeEach
  void setUp() {
    Mockito.when(keycloakClientService.getKeycloakClient()).thenReturn(
      Keycloak.getInstance(
        "http://localhost:" + keycloak.getHttpPort() + "/auth",
        "dina",
        "admin",
        "admin",
        "admin-cli"));
    Mockito.when(keycloakClientService.getRealm()).thenReturn("dina");
  }

  @Test
  void findAll_ReturnsAllRecords() {
    ResourceList<DinaUserDto> results = userRepository.findAll(new QuerySpec(DinaUserDto.class));
    Assertions.assertEquals(4, results.size());
    MatcherAssert.assertThat(
      results.stream().map(DinaUserDto::getUsername).collect(Collectors.toSet()),
      Matchers.containsInAnyOrder("cnc-cm", "admin", "user", "cnc-staff"));
  }

  @Test
  void create_RecordCreated() {
    DinaUserDto dto = newUserDto();
    DinaUserDto persisted = userRepository.create(dto);

    DinaUserDto result = userRepository.findOne(
      persisted.getInternalId(),
      new QuerySpec(DinaUserDto.class));
    Assertions.assertEquals(dto.getAgentId(), result.getAgentId());
    Assertions.assertEquals(dto.getUsername(), result.getUsername());
    Assertions.assertEquals(dto.getFirstName(), result.getFirstName());
    Assertions.assertEquals(dto.getLastName(), result.getLastName());
    Assertions.assertEquals(dto.getEmailAddress(), result.getEmailAddress());
    MatcherAssert.assertThat(
      result.getRoles(),
      Matchers.containsInAnyOrder("collection-manager"));
    MatcherAssert.assertThat(
      result.getGroups(),
      Matchers.containsInAnyOrder("/cnc/collection-manager"));
    userRepository.delete(result.getInternalId());
  }

  @Test
  void delete_RecordDeleted() {
    DinaUserDto dto = newUserDto();
    DinaUserDto persisted = userRepository.create(dto);

    DinaUserDto result = userRepository.findOne(
      persisted.getInternalId(),
      new QuerySpec(DinaUserDto.class));
    Assertions.assertNotNull(result);

    userRepository.delete(result.getInternalId());
    Assertions.assertThrows(
      NotFoundException.class,
      () -> userRepository.findOne(persisted.getInternalId(), new QuerySpec(DinaUserDto.class)));
  }

  private DinaUserDto newUserDto() {
    return DinaUserDto.builder()
      .agentId(UUID.randomUUID().toString())
      .username("new user")
      .firstName("new")
      .lastName("user")
      .emailAddress("newuser@user.com")
      .groups(List.of("/cnc/collection-manager"))
      .roles(List.of("collection-manager"))
      .build();
  }
}
