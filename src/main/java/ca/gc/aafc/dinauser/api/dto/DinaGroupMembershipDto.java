package ca.gc.aafc.dinauser.api.dto;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonApiTypeForClass(DinaGroupMembershipDto.TYPENAME)
public class DinaGroupMembershipDto implements ca.gc.aafc.dina.dto.JsonApiResource {

  public static final String TYPENAME = "group-membership";

  @com.toedter.spring.hateoas.jsonapi.JsonApiId
  private String name;

  private List<DinaUserSummaryDto> managedBy;

  @Override
  @JsonIgnore
  public String getJsonApiType() {
    return TYPENAME;
  }

  @Override
  @JsonIgnore
  public UUID getJsonApiId() {
    return UUID.fromString(name);
  }
}
