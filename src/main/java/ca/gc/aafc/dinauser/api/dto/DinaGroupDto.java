package ca.gc.aafc.dinauser.api.dto;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
  
  private String labelEn;
  private String labelFr;
}
