package ca.gc.aafc.dina.user.api.dto;

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
