package ca.gc.aafc.dinauser.api.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
import ca.gc.aafc.dina.messaging.message.MessageParam;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Notification implements DinaEntity {

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

  @Type(JsonType.class)
  @Column(columnDefinition = "jsonb")
  private Map<String, String> notificationParams;

  @NotBlank
  @Size(max = 150)
  private String title;

  @Size(max = 1000)
  private String message;

  @Type(JsonType.class)
  @Column(columnDefinition = "jsonb")
  private Map<String, List<MessageParam>> messageParams;

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
}
