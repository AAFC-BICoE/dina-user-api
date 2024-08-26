package ca.gc.aafc.dinauser.api.dto;

import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.dinauser.api.entity.ExportColumnSelection;
import ca.gc.aafc.dinauser.api.entity.UserPreference;
import io.crnk.core.resource.annotations.JsonApiField;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.PatchStrategy;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import org.javers.core.metamodel.annotation.Id;
import org.javers.core.metamodel.annotation.PropertyName;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonApiResource(type = "user-preference")
@RelatedEntity(UserPreference.class)
public class UserPreferenceDto {
  @JsonApiId
  @Id
  @PropertyName("id")
  private UUID uuid;

  @JsonApiField(patchStrategy = PatchStrategy.SET)
  private Map<String, Object> uiPreference;

  @JsonApiField(patchStrategy = PatchStrategy.SET)
  private Map<String, Object> savedSearches;

  @JsonApiField(patchStrategy = PatchStrategy.SET)
  private List<ExportColumnSelection> savedExportColumnSelection = List.of();

  private UUID userId;
  private OffsetDateTime createdOn;
}
