package ca.gc.aafc.dinauser.api.dto;

import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.dinauser.api.crudit.UserPreference;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonApiResource(type = "user-preference")
@RelatedEntity(UserPreference.class)
public class UserPreferenceDto {

  @JsonApiId
  private Integer id;
  private Map<String, String> uiPreference;

}
