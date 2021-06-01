package ca.gc.aafc.dinauser.api.openapi;

import ca.gc.aafc.dina.security.DinaRole;
import ca.gc.aafc.dina.testsupport.BaseRestAssuredTest;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.testsupport.specs.OpenAPI3Assertions;
import ca.gc.aafc.dinauser.api.DinaKeycloakTestContainer;
import ca.gc.aafc.dinauser.api.DinaUserModuleApiLauncher;
import ca.gc.aafc.dinauser.api.UserModuleTestConfiguration;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.service.KeycloakClientService;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.SneakyThrows;

import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.admin.client.Keycloak;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import javax.inject.Inject;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SpringBootTest(classes = {UserModuleTestConfiguration.class, DinaUserModuleApiLauncher.class},
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
public class UserOpenApiIT extends BaseRestAssuredTest {

  @Container
  private static final DinaKeycloakTestContainer keycloak = DinaKeycloakTestContainer.getInstance();
  public static final String USER_TYPE = "user";
  public static final String USER_ENDPOINT = "/api/v1/" + USER_TYPE;
  public static final String STUDENT_ROLE = DinaRole.STUDENT.getKeycloakRoleName();

  private static final String SPEC_HOST = "raw.githubusercontent.com";
  private static final String SPEC_PATH = "DINA-Web/user-specs/main/schema/user.yml";
  private static final URIBuilder URI_BUILDER = new URIBuilder();

  @MockBean
  private KeycloakClientService keycloakClientService;

  @Inject
  private Keycloak keycloakClient;

  @Inject
  private KeycloakSpringBootProperties properties;
  private String authUrl;

  static {
    URI_BUILDER.setScheme("https");
    URI_BUILDER.setHost(SPEC_HOST);
    URI_BUILDER.setPath(SPEC_PATH);
  }

  @BeforeAll
  static void beforeAll() {
    keycloak.start();
  }

  @BeforeEach
  void setUp() {
    mockKeycloakClienService();
    properties.setAuthServerUrl(keycloak.getAuthServerUrl());
    authUrl = keycloak.getAuthServerUrl() + "/realms/dina/protocol/openid-connect/token";
  }

  protected UserOpenApiIT() {
    super(null);
  }

  public static URL getOpenAPISpecsURL() throws URISyntaxException, MalformedURLException {
    return URI_BUILDER.build().toURL();
  }

  @Test
  @SneakyThrows
  void user_SpecValid() {
    String token = getToken(authUrl);

    DinaUserDto obj = newUserDto();
    obj.setRolesPerGroup(null);

    OpenAPI3Assertions.assertRemoteSchema(getOpenAPISpecsURL(), "User",
      sendPost(token, JsonAPITestHelper.toJsonAPIMap(USER_TYPE, newUserDto())));
  }

  private String sendPost(String token, Map<String, Object> user) {
    return newPostPatchSpec(token, user)
      .post(USER_ENDPOINT)
      .then()
      .statusCode(201)
      .extract().body().asString();
  }

  private RequestSpecification newPostPatchSpec(String token, Object body) {
    return newRequestSpec(token).contentType("application/vnd.api+json").body(body);
  }

  private RequestSpecification newRequestSpec(String token) {
    return RestAssured.given().header("Authorization", "Bearer " + token).port(testPort); 
  }

  private String getToken(String authUrl) {
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

  private void mockKeycloakClienService() {
    Mockito.when(keycloakClientService.getKeycloakClient()).thenReturn(keycloakClient);
    Mockito.when(keycloakClientService.getRealm()).thenReturn("dina");
  }

  private static DinaUserDto newUserDto() {
    return DinaUserDto.builder()
      .agentId(UUID.randomUUID().toString())
      .username(RandomStringUtils.randomAlphabetic(5).toLowerCase())
      .firstName(RandomStringUtils.randomAlphabetic(5).toLowerCase())
      .lastName(RandomStringUtils.randomAlphabetic(5).toLowerCase())
      .emailAddress(RandomStringUtils.randomAlphabetic(5).toLowerCase() + "@user.com")
      .rolesPerGroup(Map.of("cnc", Set.of(STUDENT_ROLE)))
      .build();
  }
}
