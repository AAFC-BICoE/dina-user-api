package ca.gc.aafc.dinauser.api.dto;

import java.util.Map;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;

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
  @Singular private Map<String, String> labels;

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
