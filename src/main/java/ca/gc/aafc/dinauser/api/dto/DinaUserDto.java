package ca.gc.aafc.dinauser.api.dto;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;

import ca.gc.aafc.dina.dto.RelatedEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonApiTypeForClass(DinaUserDto.TYPENAME)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RelatedEntity(DinaUserDto.class)
public class DinaUserDto implements ca.gc.aafc.dina.dto.JsonApiResource {

  public static final String TYPENAME = "user";

  @com.toedter.spring.hateoas.jsonapi.JsonApiId
  private String internalId;

  private String username;
  private String firstName;
  private String lastName;
  private String agentId;
  private String emailAddress;

  @Builder.Default
  private Map<String, Set<String>> rolesPerGroup = new HashMap<>();

  private Set<String> adminRoles;

  @Override
  @JsonIgnore
  public String getJsonApiType() {
    return TYPENAME;
  }

  @Override
  @JsonIgnore
  public UUID getJsonApiId() {
    if (StringUtils.isBlank(internalId)) {
      return null;
    }
    return UUID.fromString(internalId);
  }
}
