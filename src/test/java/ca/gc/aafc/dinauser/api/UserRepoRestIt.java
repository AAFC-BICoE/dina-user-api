package ca.gc.aafc.dinauser.api;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.repository.UserRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;

import javax.inject.Inject;

@SpringBootTest(classes = DinaUserModuleApiLauncher.class)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
public class UserRepoRestIt {

  @Autowired
  private KeycloakSpringBootProperties keycloakProperties;

  @Container
  private static final DinaKeycloakTestContainer keycloak = DinaKeycloakTestContainer.getInstance();

  @Inject
  private UserRepository userRepository;

  @BeforeAll
  static void beforeAll() {
    keycloak.start();
  }

  @BeforeEach
  void setUp() {
    keycloakProperties.setAuthServerUrl("http://localhost:"+keycloak.getHttpPort()+"/auth");
  }

  @Test
  void name() {
    System.out.println(keycloak.getExposedPorts());
    System.out.println(keycloak.getBoundPortNumbers());
    System.out.println(keycloak.getHttpPort());
    ResourceList<DinaUserDto> results = userRepository.findAll(new QuerySpec(DinaUserDto.class));
    Assertions.assertEquals(2, results.size());
  }
}
