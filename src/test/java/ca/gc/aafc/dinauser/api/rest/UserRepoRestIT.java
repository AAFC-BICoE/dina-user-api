package ca.gc.aafc.dinauser.api.rest;

import ca.gc.aafc.dina.security.DinaRole;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dinauser.api.BaseKeycloakRestIt;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class UserRepoRestIT extends BaseKeycloakRestIt {

  public static final String USER_TYPE = "user";
  public static final String USER_ENDPOINT = "/api/v1/" + USER_TYPE;
  public static final String STUDENT_ROLE = DinaRole.STUDENT.getKeycloakRoleName();

  protected UserRepoRestIT() {
    super(null);
  }

  @Test
  void patch_AddGroup_GroupAdded() {
    String token = getToken();

    DinaUserDto obj = newUserDto();
    obj.setRolesPerGroup(null);

    String id = sendPost(token, JsonAPITestHelper.toJsonAPIMap(USER_TYPE, obj));
    sendGetRequest(token, id).body("data.attributes.rolesPerGroup", Matchers.anEmptyMap());

    Map<String, Map<String, Map<String, Map<Object, Object>>>> updateData = Map.of(
      "data",
      Map.of("attributes", Map.of("rolesPerGroup", Map.of("cnc", Set.of(STUDENT_ROLE)))));

    sendPatch(token, id, updateData).statusCode(200);
    sendGetRequest(token, id).body("data.attributes.rolesPerGroup.cnc", Matchers.contains(STUDENT_ROLE));
  }

  @Test
  void patch_ReplaceGroup_GroupsReplaced() {
    String token = getToken();

    DinaUserDto obj = newUserDto();
    obj.setRolesPerGroup(Map.of("cnc", Set.of(STUDENT_ROLE), "amf", Set.of(STUDENT_ROLE)));

    String id = sendPost(token, JsonAPITestHelper.toJsonAPIMap(USER_TYPE, obj));
    sendGetRequest(token, id)
      .body("data.attributes.rolesPerGroup.cnc", Matchers.contains(STUDENT_ROLE))
      .body("data.attributes.rolesPerGroup.amf", Matchers.contains(STUDENT_ROLE));

    Map<String, Map<String, Map<String, Map<Object, Object>>>> updateData = Map.of(
      "data",
      Map.of("attributes", Map.of("rolesPerGroup", Map.of("ccfc", Set.of(STUDENT_ROLE)))));
    sendPatch(token, id, updateData).statusCode(200);

    sendGetRequest(token, id)
      .body("data.attributes.rolesPerGroup", Matchers.aMapWithSize(1))
      .body("data.attributes.rolesPerGroup.ccfc", Matchers.contains(STUDENT_ROLE));
  }

  @Test
  void patch_WhenRemovingUserRolesPerGroup_RolesPerGroupRemoved() {
    String token = getToken();

    String id = sendPost(token, JsonAPITestHelper.toJsonAPIMap(USER_TYPE, newUserDto()));

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
