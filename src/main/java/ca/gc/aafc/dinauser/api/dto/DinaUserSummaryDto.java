package ca.gc.aafc.dinauser.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DinaUserSummaryDto {
  private String username;
  private String agentId;
}
