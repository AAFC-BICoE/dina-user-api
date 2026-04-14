package ca.gc.aafc.dinauser.api.messaging;

import java.util.UUID;
import javax.inject.Named;
import javax.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.messaging.config.RabbitMQQueueProperties;
import ca.gc.aafc.dinauser.api.service.DinaUserService;
import ca.gc.aafc.dinauser.api.service.NotificationService;

import ca.gc.aafc.dinauser.api.entity.Notification;

import ca.gc.aafc.dina.messaging.message.UserMessageNotification;

@Log4j2
@Service
@RabbitListener(queues = "#{userMessageQueueProperties.getQueue()}")
@ConditionalOnProperty(prefix = "dina.messaging", name = "isConsumer", havingValue = "true")
public class UserMessageNotificationConsumer {

  private final NotificationService notificationService;
  private final DinaUserService dinaUserService;

  /**
   * Constructor
   * @param queueProperties not used directly, but we take it to make sure we have it available for receiveMessage method
   */
  public UserMessageNotificationConsumer(@Named("userMessageQueueProperties") RabbitMQQueueProperties queueProperties,
                                         NotificationService notificationService, DinaUserService dinaUserService) {
    this.notificationService = notificationService;
    this.dinaUserService = dinaUserService;
  }

  @RabbitHandler
  @Transactional
  public void handleObjectExportNotification(UserMessageNotification userMessageNotification) {
    log.info("Received message and deserialized to : {}", userMessageNotification::toString);

    var notificationBuilder = Notification.builder();
    if (userMessageNotification.getUserIdentifier() != null) {
      notificationBuilder.userIdentifier(userMessageNotification.getUserIdentifier());
    } else if (StringUtils.isNotBlank(userMessageNotification.getUsername())) {
      notificationBuilder.userIdentifier(
        UUID.fromString(
          dinaUserService.findIdentifierFromUsername(userMessageNotification.getUsername())));
    }

    Notification notification = notificationBuilder
      .title(userMessageNotification.getTitle())
      .type(userMessageNotification.getNotificationType())
      .notificationParams(userMessageNotification.getNotificationParams())
      .message(userMessageNotification.getMessage())
      .messageParams(userMessageNotification.getMessageParams())
      .status(Notification.Status.NEW)
      .group(userMessageNotification.getGroup())
      .expiresOn(userMessageNotification.getExpiresOn())
      .build();

    notificationService.create(notification);
  }
}
