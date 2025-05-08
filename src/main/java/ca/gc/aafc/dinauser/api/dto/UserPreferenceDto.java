package ca.gc.aafc.dinauser.api.dto;

import org.javers.core.metamodel.annotation.Id;
import org.javers.core.metamodel.annotation.PropertyName;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;

import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.dinauser.api.entity.ExportColumnSelection;
import ca.gc.aafc.dinauser.api.entity.UserPreference;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RelatedEntity(UserPreference.class)
@JsonApiTypeForClass(UserPreferenceDto.TYPENAME)
public class UserPreferenceDto implements ca.gc.aafc.dina.dto.JsonApiResource {

  public static final String TYPENAME = "user-preference";

  @com.toedter.spring.hateoas.jsonapi.JsonApiId
  @Id
  @PropertyName("id")
  private UUID uuid;

  private Map<String, Object> uiPreference;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Map<String, Object> savedSearches;

  private List<ExportColumnSelection> savedExportColumnSelection = List.of();

  private UUID userId;
  private OffsetDateTime createdOn;

  @Override
  @JsonIgnore
  public String getJsonApiType() {
    return TYPENAME;
  }

  @Override
  @JsonIgnore
  public UUID getJsonApiId() {
    return uuid;
  }
}
