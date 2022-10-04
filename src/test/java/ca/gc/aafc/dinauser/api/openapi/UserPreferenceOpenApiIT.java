package ca.gc.aafc.dinauser.api.openapi;

import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.testsupport.specs.OpenAPI3Assertions;
import ca.gc.aafc.dina.testsupport.specs.ValidationRestrictionOptions;
import ca.gc.aafc.dinauser.api.BaseKeycloakRestIt;
import ca.gc.aafc.dinauser.api.dto.UserPreferenceDto;
import io.restassured.response.ResponseBodyExtractionOptions;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

public class UserPreferenceOpenApiIT extends BaseKeycloakRestIt {

  public static final String USER_PREFERENCE_TYPE = "user-preference";
  public static final String USER_PREFERENCE_ENDPOINT = "/api/v1/" + USER_PREFERENCE_TYPE;
  public static final String USER_ENDPOINT = "/api/v1/" + "user";

  protected UserPreferenceOpenApiIT() {
    super(null);
  }

  @Test
  @SneakyThrows
  void userPreference_SpecValid() {
    String token = getToken();
    //we need to get the internalId of the test user
    String uuid = sendGetWithAuthOnPath(token, USER_ENDPOINT + "?filter[username]=" + USERNAME)
            .extract().jsonPath().getString("data[0].id");

    OpenAPI3Assertions.assertRemoteSchema(
      OpenApiConstants.USER_PREFERENCE_URL,
      "UserPreference",
      sendPost(
        token,
        JsonAPITestHelper.toJsonAPIMap(USER_PREFERENCE_TYPE, newUserPreferenceDto(uuid)),
        USER_PREFERENCE_ENDPOINT).asString(),
      ValidationRestrictionOptions.builder().build());

    // cleanup
    String userPrefUuid = sendGetWithAuthOnPath(token, USER_PREFERENCE_ENDPOINT + "?filter[userId]=" + uuid)
            .extract().jsonPath().getString("data[0].id");
    newRequestSpec(token).delete(USER_PREFERENCE_ENDPOINT + "/" + userPrefUuid).then().statusCode(204);
  }

  private ResponseBodyExtractionOptions sendPost(String token, Map<String, Object> user, String path) {
    return newPostPatchSpec(token, user)
      .post(path)
      .then()
      .statusCode(201)
      .extract().body();
  }

  private static UserPreferenceDto newUserPreferenceDto(String userUuid) {
    return UserPreferenceDto.builder()
      .uiPreference(Map.of("key", "value"))
      .savedSearches(Map.of("my search", Map.of("filter", "18")))
      .userId(UUID.fromString(userUuid))
      .build();
  }

}
