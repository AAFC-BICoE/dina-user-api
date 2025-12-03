package ca.gc.aafc.dinauser.api.entity;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;

import ca.gc.aafc.dina.entity.DinaEntity;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Notification implements DinaEntity {

  public enum MessageParamType { TEXT, URL }

  public enum Status { NEW, READ }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @NaturalId
  @NotNull
  @Column(name = "uuid", unique = true)
  private UUID uuid;

  private UUID userIdentifier;

  @Column(name = "_group")
  @Size(max = 50)
  private String group;

  @NotBlank
  @Size(max = 50)
  private String type;

  @NotBlank
  @Size(max = 150)
  private String title;

  @NotBlank
  @Size(max = 1000)
  private String message;

  @Type(type = "jsonb")
  private Map<String, MessageParam> messageParams;

  @NotNull
  @Enumerated(EnumType.STRING)
  private Status status;

  private OffsetDateTime expiresOn;

  @Column(name = "created_on", insertable = false, updatable = false)
  @Generated(value = GenerationTime.INSERT)
  private OffsetDateTime createdOn;

  @Override
  public String getCreatedBy() {
    return null; // Currently Unsupported
  }

  public record MessageParam(MessageParamType type, String value) {
  }
}
