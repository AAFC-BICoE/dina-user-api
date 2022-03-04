package ca.gc.aafc.dinauser.api.rest;

import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dinauser.api.BaseKeycloakRestIt;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.testsupport.fixtures.DinaUserFixture;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

public class UserPreferenceRepoIT extends BaseKeycloakRestIt {

  public static final String USER_PREFERENCE_TYPE = "user-preference";
  private static final String ENDPOINT = "/api/v1/" + USER_PREFERENCE_TYPE;

  protected UserPreferenceRepoIT() {
    super(ENDPOINT);
  }

  @Test
  void patch_AddGroup_GroupAdded() {
    String token = getToken();

    DinaUserDto obj = DinaUserFixture.newUserDto().build();
    obj.setRolesPerGroup(null);

   // String id = sendPost(token, JsonAPITestHelper.toJsonAPIMap(USER_TYPE, obj));
   // sendGetWithAuth(token, id).body("data.attributes.rolesPerGroup", Matchers.anEmptyMap());


  }
}
