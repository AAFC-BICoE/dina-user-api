package ca.gc.aafc.dinauser.api.openapi;

import ca.gc.aafc.dina.security.DinaRole;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.testsupport.specs.OpenAPI3Assertions;
import ca.gc.aafc.dina.testsupport.specs.ValidationRestrictionOptions;
import ca.gc.aafc.dinauser.api.BaseKeycloakRestIt;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.dto.UserPreferenceDto;
import io.restassured.RestAssured;
import io.restassured.response.ResponseBodyExtractionOptions;
import io.restassured.specification.RequestSpecification;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class UserPreferenceOpenApiIT extends BaseKeycloakRestIt {

  public static final String USER_PREFERENCE_TYPE = "user-preference";
  public static final String USER_PREFERENCE_ENDPOINT = "/api/v1/" + USER_PREFERENCE_TYPE;
  public static final String USER_ENDPOINT = "/api/v1/" + "user";
  public static final String STUDENT_ROLE = DinaRole.STUDENT.getKeycloakRoleName();

  protected UserPreferenceOpenApiIT() {
    super(null);
  }

  @Test
  @SneakyThrows
  void userPreference_SpecValid() {
    String token = getToken();

    String uuid = sendPost(
      token,
      JsonAPITestHelper.toJsonAPIMap("user", newUserDto()),
      USER_ENDPOINT).jsonPath().getString("data.id");

    OpenAPI3Assertions.assertRemoteSchema(
      OpenApiConstants.USER_PREFERENCE_URL,
      "UserPreference",
      sendPost(
        token,
        JsonAPITestHelper.toJsonAPIMap(USER_PREFERENCE_TYPE, newUserPreferenceto(uuid)),
        USER_PREFERENCE_ENDPOINT).asString(),
      ValidationRestrictionOptions.builder().allowAdditionalFields(true).build());
  }

  private ResponseBodyExtractionOptions sendPost(String token, Map<String, Object> user, String path) {
    return newPostPatchSpec(token, user)
      .post(path)
      .then()
      .statusCode(201)
      .extract().body();
  }

  private RequestSpecification newPostPatchSpec(String token, Object body) {
    return newRequestSpec(token).contentType("application/vnd.api+json").body(body);
  }

  private RequestSpecification newRequestSpec(String token) {
    return RestAssured.given().header("Authorization", "Bearer " + token).port(testPort);
  }

  private static UserPreferenceDto newUserPreferenceto(String uuid) {
    return UserPreferenceDto.builder()
      .uiPreference(Map.of("key", "value"))
      .userId(UUID.fromString(uuid))
      .build();
  }

  private DinaUserDto newUserDto() {
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
