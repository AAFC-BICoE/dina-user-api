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
import io.restassured.specification.RequestSpecification;
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
public class UserRestIt extends BaseRestAssuredTest {

  @Container
  private static final DinaKeycloakTestContainer keycloak = DinaKeycloakTestContainer.getInstance();
  public static final String USER_ENDPOINT = "/api/v1/user";

  @MockBean
  private KeycloakClientService keycloakClientService;

  @Inject
  private Keycloak keycloakClient;

  @Inject
  private KeycloakSpringBootProperties properties;

  @BeforeAll
  static void beforeAll() {
    keycloak.start();
  }

  @BeforeEach
  void setUp() {
    mockKeycloakClienService();
    properties.setAuthServerUrl(keycloak.getAuthServerUrl());
  }

  protected UserRestIt() {
    super(null);
  }

  @Test
  void patch_WhenRemovingUserRolesPerGroup_RolesPerGroupRemoved() {
    String authUrl = keycloak.getAuthServerUrl() + "/realms/dina/protocol/openid-connect/token";

    String token = getToken(authUrl);

    DinaUserDto obj = newUserDto();

    String id = getAuthorizationRequest(token)
      .contentType("application/vnd.api+json")
      .body(JsonAPITestHelper.toJsonAPIMap("user", obj))
      .post(USER_ENDPOINT)
      .then()
      .statusCode(201)
      .extract().body().jsonPath().getString("data.id");

    obj.setRolesPerGroup(Map.of());

    getAuthorizationRequest(token)
      .contentType("application/vnd.api+json")
      .body(JsonAPITestHelper.toJsonAPIMap("user", obj))
      .patch(USER_ENDPOINT + "/" + id)
      .then().log().all(true);//TODO remove line

    getAuthorizationRequest(token)
      .get(USER_ENDPOINT + "/" + id)
      .then()
      .log().all(true)//TODO remove line
      .body("data.attributes.rolesPerGroup", Matchers.anEmptyMap());
  }

  private RequestSpecification getAuthorizationRequest(String token) {
    return RestAssured.given()
      .header("Authorization", "Bearer " + token)
      .port(testPort);
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
