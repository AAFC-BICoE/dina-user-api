package ca.gc.aafc.dina.user.api.dto;

import java.util.Map;

import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;

import ca.gc.aafc.dina.jsonapi.JsonApiImmutable;
import ca.gc.aafc.dina.security.DinaRole;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonApiTypeForClass(DinaGroupDto.TYPENAME)
public class DinaGroupDto implements ca.gc.aafc.dina.dto.JsonApiResource {

  public static final String TYPENAME = "group";

  @com.toedter.spring.hateoas.jsonapi.JsonApiId
  private String internalId;

  private String name;
  private String path;
  
  /** map from [ISO language code -> value] */
  @Singular
  private Map<String, String> labels;

  // Possible roles (subgroups) available for that group. Some group may not have all available roles
  // available
  @JsonApiImmutable({JsonApiImmutable.ImmutableOn.CREATE, JsonApiImmutable.ImmutableOn.UPDATE})
  private Set<DinaRole> roles;

  @Override
  @JsonIgnore
  public String getJsonApiType() {
    return TYPENAME;
  }

  @Override
  @JsonIgnore
  public UUID getJsonApiId() {
    return UUID.fromString(internalId);
  }
}
