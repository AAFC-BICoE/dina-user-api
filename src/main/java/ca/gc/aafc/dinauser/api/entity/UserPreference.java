package ca.gc.aafc.dinauser.api.entity;

import ca.gc.aafc.dina.entity.DinaEntity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
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

  @Type(type = "jsonb")
  @Column(name = "ui_preference", columnDefinition = "jsonb")
  private Map<String, Object> uiPreference;

  @Type(type = "jsonb")
  @Column(name = "saved_searches", columnDefinition = "jsonb")
  private Map<String, Object> savedSearches;

  @Type(type = "jsonb")
  @Column(name = "saved_export_column_selection", columnDefinition = "jsonb")
  private List<ExportColumnSelection> savedExportColumnSelection = List.of();

  @Column(name = "created_on", insertable = false, updatable = false)
  @Generated(value = GenerationTime.INSERT)
  private OffsetDateTime createdOn;

  @Override
  public String getCreatedBy() {
    return null; // Currently Unsupported
  }

}
