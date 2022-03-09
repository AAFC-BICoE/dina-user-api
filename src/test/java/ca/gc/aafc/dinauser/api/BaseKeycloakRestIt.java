package ca.gc.aafc.dinauser.api;

import ca.gc.aafc.dina.testsupport.BaseRestAssuredTest;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dinauser.api.service.KeycloakClientService;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.admin.client.Keycloak;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;

import javax.inject.Inject;
import java.util.Map;

@SpringBootTest(classes = {UserModuleTestConfiguration.class, DinaUserModuleApiLauncher.class},
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
public class BaseKeycloakRestIt extends BaseRestAssuredTest {

  @Container
  private static final DinaKeycloakTestContainer KEYCLOAK = DinaKeycloakTestContainer.getInstance();

  @MockBean
  private KeycloakClientService keycloakClientService;

  @Inject
  private Keycloak keycloakClient;

  @Inject
  private KeycloakSpringBootProperties properties;
  private String authUrl;

  protected BaseKeycloakRestIt(String basePath) {
    super(basePath);
  }

  @BeforeAll
  static void beforeAll() {
    KEYCLOAK.start();
  }

  @BeforeEach
  void setUp() {
    mockKeycloakClientService();
    properties.setAuthServerUrl(KEYCLOAK.getAuthServerUrl());
    authUrl = KEYCLOAK.getAuthServerUrl() + "/realms/dina/protocol/openid-connect/token";
  }

  private void mockKeycloakClientService() {
    Mockito.when(keycloakClientService.getKeycloakClient()).thenReturn(keycloakClient);
    Mockito.when(keycloakClientService.getRealm()).thenReturn("dina");
  }

  protected String getToken() {
    return RestAssured.given()
      .contentType("application/x-www-form-urlencoded")
      .formParam("grant_type", "password")
      .formParam("client_id", "objectstore")
      .formParam("password", "cnc-cm")
      .formParam("username", "cnc-cm")
      .post(authUrl)
      .then()
      .statusCode(200)
      .extract().body().jsonPath().getString("access_token");
  }

  protected RequestSpecification newPostPatchSpec(String token, Object body) {
    return newRequestSpec(token).contentType("application/vnd.api+json").body(body);
  }

  protected RequestSpecification newRequestSpec(String token) {
    return RestAssured.given().header("Authorization", "Bearer " + token).port(testPort);
  }

  protected String sendPostWithAuth(String token, Map<String, Object> body) {
    return sendPostWithAuth(token, basePath, body);
  }

  protected String sendPostWithAuth(String token, String path, Map<String, Object> body) {
    return newPostPatchSpec(token, body)
        .post(path)
        .then()
        .statusCode(201)
        .extract().body().jsonPath().getString("data.id");
  }

  protected ValidatableResponse sendGetWithAuth(String token, String id) {
    return newRequestSpec(token).get(getEndpointWithId(id)).then();
  }

  /**
   * Send a GET at {@link #basePath}
   * @param token
   * @return
   */
  protected ValidatableResponse sendGetWithAuth(String token) {
    return newRequestSpec(token).get(basePath).then();
  }

  protected ValidatableResponse sendPatchWithAuth(
      String token,
      String id,
      Map<String, ?> body
  ) {
    return newPostPatchSpec(token, body).patch(getEndpointWithId(id)).then();
  }

  private String getEndpointWithId(String id) {
    return StringUtils.appendIfMissing(basePath, "/") + id;
  }

}
