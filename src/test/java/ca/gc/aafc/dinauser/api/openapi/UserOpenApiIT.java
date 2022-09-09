package ca.gc.aafc.dinauser.api.openapi;

import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.testsupport.specs.OpenAPI3Assertions;
import ca.gc.aafc.dinauser.api.BaseKeycloakRestIt;
import ca.gc.aafc.dinauser.api.testsupport.fixtures.DinaUserFixture;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class UserOpenApiIT extends BaseKeycloakRestIt {

  public static final String USER_TYPE = "user";
  public static final String USER_ENDPOINT = "/api/v1/" + USER_TYPE;

  protected UserOpenApiIT() {
    super(null);
  }

  @Test
  @SneakyThrows
  void user_SpecValid() {
    String token = getToken();

    OpenAPI3Assertions.assertRemoteSchema(OpenApiConstants.USER_URL, "User",
      sendPost(token, JsonAPITestHelper.toJsonAPIMap(USER_TYPE,
              DinaUserFixture.newUserDto().build())));
  }

  private String sendPost(String token, Map<String, Object> user) {
    return newPostPatchSpec(token, user)
      .post(USER_ENDPOINT)
      .then()
      .statusCode(201)
      .extract().body().asString();
  }

}
