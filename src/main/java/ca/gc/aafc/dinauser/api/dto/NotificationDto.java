package ca.gc.aafc.dinauser.api.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.javers.core.metamodel.annotation.Id;
import org.javers.core.metamodel.annotation.PropertyName;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;

import ca.gc.aafc.dina.dto.JsonApiResource;
import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.dinauser.api.entity.Notification;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RelatedEntity(Notification.class)
@JsonApiTypeForClass(NotificationDto.TYPENAME)
public class NotificationDto implements JsonApiResource {

  public static final String TYPENAME = "notification";

  @JsonApiId
  @Id
  @PropertyName("id")
  private UUID uuid;

  private UUID userIdentifier;
  private String group;

  private String type;
  private String title;
  private String message;
  private Map<String, Notification.MessageParam> messageParams;

  private Notification.Status status;

  private OffsetDateTime expiresOn;
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
