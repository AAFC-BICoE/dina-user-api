package ca.gc.aafc.dinauser.api.openapi;

import ca.gc.aafc.dina.security.DinaRole;
import ca.gc.aafc.dina.testsupport.BaseRestAssuredTest;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.testsupport.specs.OpenAPI3Assertions;
import ca.gc.aafc.dinauser.api.DinaKeycloakTestContainer;
import ca.gc.aafc.dinauser.api.DinaUserModuleApiLauncher;
import ca.gc.aafc.dinauser.api.UserModuleTestConfiguration;
import ca.gc.aafc.dinauser.api.dto.DinaGroupDto;
import ca.gc.aafc.dinauser.api.service.KeycloakClientService;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import lombok.SneakyThrows;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.hamcrest.Matchers;
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
public class GroupOpenApiIT extends BaseRestAssuredTest {

  @Container
  private static final DinaKeycloakTestContainer keycloak = DinaKeycloakTestContainer.getInstance();
  public static final String GROUP_TYPE = "group";
  public static final String GROUP_ENDPOINT = "/api/v1/" + GROUP_TYPE;
  public static final String STUDENT_ROLE = DinaRole.STUDENT.getKeycloakRoleName();

  private static final String SPEC_HOST = "raw.githubusercontent.com";
  private static final String SPEC_PATH = "luusteve/user-specs/22866_update_open_api_specs-Groups/schema/group.yml";
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

  protected GroupOpenApiIT() {
    super(null);
  }

  public static URL getOpenAPISpecsURL() throws URISyntaxException, MalformedURLException {
    return URI_BUILDER.build().toURL();
  }

  @Test
  @SneakyThrows
  void patch_AddGroup_GroupAdded() {
    String token = getToken(authUrl);

    OpenAPI3Assertions.assertRemoteSchema(getOpenAPISpecsURL(), "Group",
      sendPost(token, JsonAPITestHelper.toJsonAPIMap(GROUP_TYPE, JsonAPITestHelper.toAttributeMap(newGroupDto()))));
  }

  private String sendPost(String token, Map<String, Object> user) {
    return newPostPatchSpec(token, user)
      .post(GROUP_ENDPOINT)
      .then()
      .statusCode(201)
      .extract().body().jsonPath().getString("");
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

  private static DinaGroupDto newGroupDto() {
    return DinaGroupDto.builder()
      .name("aafc")
      .path("path")
      .labels(Map.of("fr", "French"))
      .build();
  }
}
