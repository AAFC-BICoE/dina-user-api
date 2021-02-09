package ca.gc.aafc.dinauser.api.entity;

import ca.gc.aafc.dina.entity.DinaEntity;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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

  @NotNull
  @Column(name = "user_id", unique = true)
  private UUID userId;

  @Type(type = "jsonb")
  @Column(name = "ui_preference", columnDefinition = "jsonb")
  private Map<String, String> uiPreference;

  @Override
  public String getCreatedBy() {
    return null; // Currently Unsupported
  }

  @Override
  public OffsetDateTime getCreatedOn() {
    return null; // Currently Unsupported
  }

}
