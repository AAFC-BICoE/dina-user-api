package ca.gc.aafc.dinauser.api.testsupport.fixtures;

import ca.gc.aafc.dinauser.api.dto.UserPreferenceDto;
import ca.gc.aafc.dinauser.api.entity.ExportColumnSelection;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserPreferenceFixture {

  public static UserPreferenceDto.UserPreferenceDtoBuilder newUserPreferenceDto(
    UUID expectedUserId) {
    return UserPreferenceDto.builder()
      .uiPreference(Map.of("key", "value"))
      .savedExportColumnSelection(List.of(ExportColumnSelection.builder()
        .name("my export columns").component("material-sample")
        .columns(List.of("col1", "col2"))
        .columnAliases(List.of("c1", "c2"))
        .build()))
      .userId(expectedUserId);
  }

}
