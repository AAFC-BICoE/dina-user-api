package ca.gc.aafc.dinauser.api.dto;

import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.dina.entity.DinaEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.crnk.core.resource.annotations.JsonApiField;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.PatchStrategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@JsonApiResource(type = "user")
@Data
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
  @JsonApiField(patchStrategy = PatchStrategy.SET)
  private Map<String, Set<String>> rolesPerGroup = new HashMap<>();

  private Set<String> adminRoles;

  @Override
  @JsonIgnore
  public Integer getId() {
    return null;
  }

  @Override
  @JsonIgnore
  public UUID getUuid() {
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
