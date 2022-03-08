package ca.gc.aafc.dinauser.api.rest;

import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dinauser.api.BaseKeycloakRestIt;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.testsupport.fixtures.DinaUserFixture;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

public class UserRepoRestIT extends BaseKeycloakRestIt {

  public static final String USER_TYPE = "user";
  public static final String USER_ENDPOINT = "/api/v1/" + USER_TYPE;

  protected UserRepoRestIT() {
    super(USER_ENDPOINT);
  }

  @Test
  void patch_AddGroup_GroupAdded() {
    String token = getToken();

    DinaUserDto obj = DinaUserFixture.newUserDto().build();
    obj.setRolesPerGroup(null);

    String id = sendPostWithAuth(token, JsonAPITestHelper.toJsonAPIMap(USER_TYPE, obj));
    sendGetWithAuth(token, id).body("data.attributes.rolesPerGroup", Matchers.anEmptyMap());

    Map<String, Map<String, Map<String, Map<Object, Object>>>> updateData = Map.of(
      "data",
      Map.of("attributes", Map.of("rolesPerGroup", Map.of("cnc", Set.of(DinaUserFixture.STUDENT_ROLE)))));

    sendPatchWithAuth(token, id, updateData).statusCode(200);
    sendGetWithAuth(token, id).body("data.attributes.rolesPerGroup.cnc", Matchers.contains(DinaUserFixture.STUDENT_ROLE));
  }

  @Test
  void patch_ReplaceGroup_GroupsReplaced() {
    String token = getToken();

    DinaUserDto obj = DinaUserFixture.newUserDto().build();
    obj.setRolesPerGroup(Map.of("cnc", Set.of(DinaUserFixture.STUDENT_ROLE), "amf", Set.of(DinaUserFixture.STUDENT_ROLE)));

    String id = sendPostWithAuth(token, JsonAPITestHelper.toJsonAPIMap(USER_TYPE, obj));
    sendGetWithAuth(token, id)
      .body("data.attributes.rolesPerGroup.cnc", Matchers.contains(DinaUserFixture.STUDENT_ROLE))
      .body("data.attributes.rolesPerGroup.amf", Matchers.contains(DinaUserFixture.STUDENT_ROLE));

    Map<String, Map<String, Map<String, Map<Object, Object>>>> updateData = Map.of(
      "data",
      Map.of("attributes", Map.of("rolesPerGroup", Map.of("ccfc", Set.of(DinaUserFixture.STUDENT_ROLE)))));
    sendPatchWithAuth(token, id, updateData).statusCode(200);

    sendGetWithAuth(token, id)
      .body("data.attributes.rolesPerGroup", Matchers.aMapWithSize(1))
      .body("data.attributes.rolesPerGroup.ccfc", Matchers.contains(DinaUserFixture.STUDENT_ROLE));
  }

  @Test
  void patch_WhenRemovingUserRolesPerGroup_RolesPerGroupRemoved() {
    String token = getToken();

    String id = sendPostWithAuth(token, JsonAPITestHelper.toJsonAPIMap(USER_TYPE,
        DinaUserFixture.newUserDto().build()));

    Map<String, Map<String, Map<String, Map<Object, Object>>>> updateData = Map.of(
      "data",
      Map.of("attributes", Map.of("rolesPerGroup", Map.of())));
    sendPatchWithAuth(token, id, updateData).statusCode(200);

    sendGetWithAuth(token, id).body("data.attributes.rolesPerGroup", Matchers.anEmptyMap());
  }

}
