package ca.gc.aafc.dinauser.api.entity;

import ca.gc.aafc.dina.entity.DinaEntity;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.Map;

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

  @NotBlank
  @Column(name = "user_id", unique = true)
  private String userId;

  @Type(type = "jsonb")
  @Column(name = "ui_preference", columnDefinition = "jsonb")
  private Map<String, String> uiPreference;

  @Column(name = "created_on", insertable = false, updatable = false)
  @Generated(value = GenerationTime.INSERT)
  private OffsetDateTime createdOn;

  @Override
  public String getCreatedBy() {
    return null; // Currently Unsupported
  }

}
