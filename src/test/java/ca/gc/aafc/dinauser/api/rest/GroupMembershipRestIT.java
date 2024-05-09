package ca.gc.aafc.dinauser.api.rest;

import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.Test;

import ca.gc.aafc.dinauser.api.BaseKeycloakRestIt;

public class GroupMembershipRestIT extends BaseKeycloakRestIt {

  private static final String TYPE = "group-membership";
  private static final String ENDPOINT = "/api/v1/" + TYPE;

  protected GroupMembershipRestIT() {
    super(ENDPOINT);
  }

  @Test
  void groupMembership_onGet_groupReturned() {
    String token = getToken();
    sendGetWithAuth(token, "cnc").body("data.attributes.managedBy[0]", IsMapContaining.hasEntry("username", "cnc-su")).statusCode(200);
  }

}
