package ca.gc.aafc.dinauser.api;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.repository.UserRepository;
import ca.gc.aafc.dinauser.api.service.KeycloakClientService;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.admin.client.Keycloak;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;

import javax.inject.Inject;

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
  void name() {
    ResourceList<DinaUserDto> results = userRepository.findAll(new QuerySpec(DinaUserDto.class));
    Assertions.assertEquals(4, results.size());
  }
}
