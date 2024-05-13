package ca.gc.aafc.dinauser.api.dto;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonApiResource(type = "group-membership")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DinaGroupMembershipDto {

  @JsonApiId
  private String name;

  private List<DinaUserSummaryDto> managedBy;

}
