package ca.gc.aafc.dina.user.api.entity;

import ca.gc.aafc.dina.entity.DinaEntity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import java.util.List;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class UserPreference implements DinaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @NaturalId
  @NotNull
  @Column(name = "uuid", unique = true)
  private UUID uuid;

  @NotNull
  @Column(name = "user_id", unique = true)
  private UUID userId;

  @Type(JsonType.class)
  @Column(name = "ui_preference", columnDefinition = "jsonb")
  private Map<String, Object> uiPreference;

  @Type(JsonType.class)
  @Column(name = "saved_searches", columnDefinition = "jsonb")
  private Map<String, Object> savedSearches;

  @Type(JsonType.class)
  @Column(name = "saved_export_column_selection", columnDefinition = "jsonb")
  @Valid
  private List<ExportColumnSelection> savedExportColumnSelection = List.of();

  @Column(name = "created_on", insertable = false, updatable = false)
  @Generated(value = GenerationTime.INSERT)
  private OffsetDateTime createdOn;

  @Override
  public String getCreatedBy() {
    return null; // Currently Unsupported
  }

}
