package ca.gc.aafc.dinauser.api.rest;

import ca.gc.aafc.dinauser.api.BaseKeycloakRestIt;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.Test;

public class GroupRestIT extends BaseKeycloakRestIt {
  private static final String ENDPOINT = "/api/v1/group";

  protected GroupRestIT() {
    super(ENDPOINT);
  }

  @Test
  void get_GetGroup_groupsReturned() {
    String token = getToken();
    sendGetWithAuth(token).body("meta", IsMapContaining.hasKey("moduleVersion")).statusCode(200);
  }
}
