package ca.gc.aafc.dinauser.api.rest;

import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dinauser.api.BaseKeycloakRestIt;
import ca.gc.aafc.dinauser.api.dto.DinaGroupDto;

import org.hamcrest.Matchers;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GroupRestIT extends BaseKeycloakRestIt {

  private static final String TYPE = "group";
  private static final String ENDPOINT = "/api/v1/" + TYPE;

  protected GroupRestIT() {
    super(ENDPOINT);
  }

  @Test
  void get_GetGroup_groupReturned() {
    String token = getToken(DINA_ADMIN_USERNAME, DINA_ADMIN_USERNAME);
    DinaGroupDto dto = createGroupDto();
    dto.setName("test-group");
    String strId = sendPostWithAuth(token, JsonAPITestHelper.toJsonAPIMap(TYPE, dto));
    assertNotNull(strId);

    // Check that the group is returned with the correct attributes
    sendGetWithAuth(token, strId)
      .body("data.id", Matchers.equalTo(strId))
      .body("data.attributes.name", Matchers.equalTo(dto.getName()))
      .body("meta", IsMapContaining.hasKey("moduleVersion"))
      .statusCode(200);
  }

  @Test
  void group_OnNewGroup_groupCreated() {
    String token = getToken(DINA_ADMIN_USERNAME, DINA_ADMIN_USERNAME);
    DinaGroupDto dto = createGroupDto();

    String strId = sendPostWithAuth(token, JsonAPITestHelper.toJsonAPIMap(TYPE, dto));
    assertNotNull(strId);
  }

  private DinaGroupDto createGroupDto() {
    DinaGroupDto dto = DinaGroupDto.builder()
      .name("my-new-group")
      .label("en", "my new group")
      .label("fr", "mon nouveau groupe")
      .build();
    return dto;
  }
}
