package ca.gc.aafc.dinauser.api.openapi;

import ca.gc.aafc.dina.security.DinaRole;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.testsupport.specs.OpenAPI3Assertions;
import ca.gc.aafc.dinauser.api.BaseKeycloakRestIt;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class UserOpenApiIT extends BaseKeycloakRestIt {

  public static final String USER_TYPE = "user";
  public static final String USER_ENDPOINT = "/api/v1/" + USER_TYPE;
  public static final String STUDENT_ROLE = DinaRole.STUDENT.getKeycloakRoleName();

  protected UserOpenApiIT() {
    super(null);
  }

  @Test
  @SneakyThrows
  void user_SpecValid() {
    String token = getToken();

    DinaUserDto obj = newUserDto();
    obj.setRolesPerGroup(null);

    OpenAPI3Assertions.assertRemoteSchema(OpenApiConstants.USER_URL, "User",
      sendPost(token, JsonAPITestHelper.toJsonAPIMap(USER_TYPE, newUserDto())));
  }

  private String sendPost(String token, Map<String, Object> user) {
    return newPostPatchSpec(token, user)
      .post(USER_ENDPOINT)
      .then()
      .statusCode(201)
      .extract().body().asString();
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
