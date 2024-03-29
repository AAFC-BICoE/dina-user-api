package ca.gc.aafc.dinauser.api.rest;

import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dinauser.api.BaseKeycloakRestIt;
import ca.gc.aafc.dinauser.api.TestResourceHelper;
import ca.gc.aafc.dinauser.api.dto.UserPreferenceDto;
import ca.gc.aafc.dinauser.api.repository.UserPreferenceRepositoryIT;
import ca.gc.aafc.dinauser.api.testsupport.fixtures.UserPreferenceFixture;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;

public class UserPreferenceRepoIT extends BaseKeycloakRestIt {

  public static final String USER_PREFERENCE_TYPE = "user-preference";
  private static final String ENDPOINT = "/api/v1/" + USER_PREFERENCE_TYPE;

  protected UserPreferenceRepoIT() {
    super(ENDPOINT);
  }

  @Test
  void patch_SavedSearched_SavedSearchesPatched() {
    String token = getToken();
    // Get the internalId of the test user
    String userId = sendGetWithAuthOnPath(token, UserRepoRestIT.USER_ENDPOINT + "?filter[username]=" + USERNAME)
            .extract().jsonPath().getString("data[0].id");

    UserPreferenceDto dto = UserPreferenceFixture.newUserPreferenceDto(UUID.fromString(userId))
        .savedSearches(TestResourceHelper.readContentAsJsonMap(
            UserPreferenceRepositoryIT.TEST_RESOURCE_PATH + UserPreferenceRepositoryIT.UPDATED_SAVED_SEARCH_RESOURCE))
        .build();

    String prefId = sendPostWithAuth(token, ENDPOINT, JsonAPITestHelper.toJsonAPIMap(USER_PREFERENCE_TYPE, dto));

    //remove the saved searches
    dto.setSavedSearches(Collections.emptyMap());

    sendPatchWithAuth(token, prefId, JsonAPITestHelper.toJsonAPIMap(USER_PREFERENCE_TYPE, dto));
    sendGetWithAuth(token, prefId).body("data.attributes.savedSearches", Matchers.anEmptyMap());

    // cleanup
    newRequestSpec(token).delete(ENDPOINT + "/" + prefId).then().statusCode(204);
  }
}
