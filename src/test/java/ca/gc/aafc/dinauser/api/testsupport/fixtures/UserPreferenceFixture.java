package ca.gc.aafc.dinauser.api.testsupport.fixtures;

import ca.gc.aafc.dinauser.api.dto.UserPreferenceDto;

import java.util.Map;
import java.util.UUID;

public class UserPreferenceFixture {

  public static UserPreferenceDto.UserPreferenceDtoBuilder newUserPreferenceDto(UUID expectedUserId) {
    return UserPreferenceDto.builder()
        .uiPreference(Map.of("key", "value"))
        .userId(expectedUserId);
  }

}
