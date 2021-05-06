package ca.gc.aafc.dinauser.api.rest;

import ca.gc.aafc.dina.testsupport.BaseRestAssuredTest;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dinauser.api.DinaKeycloakTestContainer;
import ca.gc.aafc.dinauser.api.DinaUserModuleApiLauncher;
import ca.gc.aafc.dinauser.api.UserModuleTestConfiguration;
import ca.gc.aafc.dinauser.api.service.KeycloakClientService;
import io.restassured.RestAssured;
import io.restassured.http.Header;
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

@SpringBootTest(classes = {UserModuleTestConfiguration.class, DinaUserModuleApiLauncher.class},
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
public class UserRestIt extends BaseRestAssuredTest {
  private static final Header CRNK_HEADER = new Header("crnk-compact", "true");
  @Container
  private static final DinaKeycloakTestContainer keycloak = DinaKeycloakTestContainer.getInstance();

  @MockBean
  private KeycloakClientService keycloakClientService;

  @Inject
  private Keycloak keycloakClient;

  @BeforeAll
  static void beforeAll() {
    keycloak.start();
  }

  @BeforeEach
  void setUp() {
    mockKeycloakClienService();
  }

  protected UserRestIt() {
    super(null);
  }

  @Test
  void findAll_ReturnsAllConfigs() {
    String authUrl = keycloak.getAuthServerUrl() + "/realms/dina/protocol/openid-connect/token";

    String token = RestAssured.given()
      .contentType("application/x-www-form-urlencoded")
      .formParam("grant_type", "password")
      .formParam("client_id", "objectstore")
      .formParam("password", "cnc-cm")
      .formParam("username", "cnc-cm")
      .post(authUrl)
      .then()
      .statusCode(200)
      .extract().body().jsonPath().getString("access_token");

    System.out.println(token);

    RestAssured.given().auth().oauth2(token)
      .port(testPort)
      .get("/user")
      .then().log().all(true);

  }

  private void mockKeycloakClienService() {
    Mockito.when(keycloakClientService.getKeycloakClient()).thenReturn(keycloakClient);
    Mockito.when(keycloakClientService.getRealm()).thenReturn("dina");
  }
}
