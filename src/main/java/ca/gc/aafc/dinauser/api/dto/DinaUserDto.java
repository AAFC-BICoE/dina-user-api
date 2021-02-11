package ca.gc.aafc.dinauser.api.dto;

import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.dina.entity.DinaEntity;
import ca.gc.aafc.dina.security.DinaRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonApiResource(type = "user")
@Data
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RelatedEntity(DinaUserDto.class)
public class DinaUserDto implements DinaEntity {

  @JsonApiId
  private String internalId;

  private String username;
  private String firstName;
  private String lastName;
  private String agentId;
  private String emailAddress;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Builder.Default
  private List<String> roles = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Builder.Default
  private List<String> groups = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Builder.Default
  private  Map<String, Set<DinaRole>> rolesPerGroup = new HashMap<>();

  @Override
  @JsonIgnore
  public Integer getId() {
    return null;
  }

  @Override
  @JsonIgnore
  public String getCreatedBy() {
    return null;
  }

  @Override
  @JsonIgnore
  public OffsetDateTime getCreatedOn() {
    return null;
  }
}
