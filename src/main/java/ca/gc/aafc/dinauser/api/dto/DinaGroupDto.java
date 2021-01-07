package ca.gc.aafc.dinauser.api.dto;

import java.util.Map;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@JsonApiResource(type = "group")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DinaGroupDto {
  
  @JsonApiId
  private String internalId;

  private String name;
  private String path;
  
  /** map from [ISO language code -> value] */
  @Singular private Map<String, String> labels;
}
