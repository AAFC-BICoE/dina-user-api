package ca.gc.aafc.dinauser.api.dto;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@JsonApiResource(type = "user")
@Data
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DinaUserDto {

  @JsonApiId
  private String internalId;

  private String username;
  private String firstName;
  private String lastName;
  private String agentId;
  private String emailAddress;
  private final List<String> roles = new ArrayList<>();
  private final List<String> groups = new ArrayList<>();

}
