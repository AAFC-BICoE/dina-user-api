package ca.gc.aafc.dinauser.api.rest;

import ca.gc.aafc.dina.testsupport.BaseRestAssuredTest;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dinauser.api.DinaKeycloakTestContainer;
import ca.gc.aafc.dinauser.api.DinaUserModuleApiLauncher;
import ca.gc.aafc.dinauser.api.UserModuleTestConfiguration;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.service.KeycloakClientService;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SpringBootTest(classes = {UserModuleTestConfiguration.class, DinaUserModuleApiLauncher.class},
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
public class UserRepoRestIT extends BaseRestAssuredTest {

  @Container
  private static final DinaKeycloakTestContainer keycloak = DinaKeycloakTestContainer.getInstance();
  public static final String USER_ENDPOINT = "/api/v1/user";

  @MockBean
  private KeycloakClientService keycloakClientService;

  @Inject
  private Keycloak keycloakClient;

  @Inject
  private KeycloakSpringBootProperties properties;
  private String authUrl;

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

  protected UserRepoRestIT() {
    super(null);
  }

  @Test
  void patch_AddGroup_GroupAdded() {
    String token = getToken(authUrl);

    DinaUserDto obj = newUserDto();
    obj.setRolesPerGroup(null);

    String id = sendPost(token, JsonAPITestHelper.toJsonAPIMap("user", obj));
    sendGetRequest(token, id).body("data.attributes.rolesPerGroup", Matchers.anEmptyMap());

    Map<String, Map<String, Map<String, Map<Object, Object>>>> updateData = Map.of(
      "data",
      Map.of("attributes", Map.of("rolesPerGroup", Map.of("cnc", Set.of("student")))));

    sendPatch(token, id, updateData).statusCode(200);
    sendGetRequest(token, id).body("data.attributes.rolesPerGroup.cnc", Matchers.contains("student"));
  }

  @Test
  void patch_WhenRemovingUserRolesPerGroup_RolesPerGroupRemoved() {
    String token = getToken(authUrl);

    Map<String, Object> user = JsonAPITestHelper.toJsonAPIMap("user", newUserDto());
    String id = sendPost(token, user);

    Map<String, Map<String, Map<String, Map<Object, Object>>>> updateData = Map.of(
      "data",
      Map.of("attributes", Map.of("rolesPerGroup", Map.of())));
    sendPatch(token, id, updateData).statusCode(200);

    sendGetRequest(token, id).body("data.attributes.rolesPerGroup", Matchers.anEmptyMap());
  }

  private ValidatableResponse sendGetRequest(String token, String id) {
    return newRequestSpec(token).get(getUserEndpointWithId(id)).then();
  }

  private ValidatableResponse sendPatch(
    String token,
    String id,
    Map<String, Map<String, Map<String, Map<Object, Object>>>> updateData
  ) {
    return newPostPatchSpec(token, updateData).patch(getUserEndpointWithId(id)).then();
  }

  private static String getUserEndpointWithId(String id) {
    return StringUtils.appendIfMissing(USER_ENDPOINT, "/") + id;
  }

  private String sendPost(String token, Map<String, Object> user) {
    return newPostPatchSpec(token, user)
      .post(USER_ENDPOINT)
      .then()
      .statusCode(201)
      .extract().body().jsonPath().getString("data.id");
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
      .rolesPerGroup(Map.of("cnc", Set.of("student")))
      .build();
  }
}
