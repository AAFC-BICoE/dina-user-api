package ca.gc.aafc.dinauser.api.messaging;

import javax.inject.Named;
import javax.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.messaging.config.RabbitMQQueueProperties;
import ca.gc.aafc.dinauser.api.service.NotificationService;

import ca.gc.aafc.dinauser.api.entity.Notification;

import ca.gc.aafc.dina.messaging.message.UserMessageNotification;

@Log4j2
@Service
@RabbitListener(queues = "#{userMessageQueueProperties.getQueue()}")
@ConditionalOnProperty(prefix = "dina.messaging", name = "isConsumer", havingValue = "true")
public class UserMessageNotificationConsumer {

  private final NotificationService notificationService;

  /**
   * Constructor
   * @param queueProperties not used directly, but we take it to make sure we have it available for receiveMessage method
   */
  public UserMessageNotificationConsumer(@Named("exportQueueProperties") RabbitMQQueueProperties queueProperties,
                                         NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @RabbitHandler
  @Transactional
  public void handleObjectExportNotification(UserMessageNotification userMessageNotification) {
    log.info("Received message and deserialized to : {}", userMessageNotification::toString);

    Notification notification = Notification.builder()
      .title(userMessageNotification.getTitle())
      .message(userMessageNotification.getMessage())
      .messageParams(userMessageNotification.getMessageParams())
      .type(userMessageNotification.getMessage())
      .status(Notification.Status.NEW)
      .userIdentifier(userMessageNotification.getUserIdentifier())
      .group(userMessageNotification.getGroup())
      .expiresOn(userMessageNotification.getExpiresOn())
      .build();

    notificationService.create(notification);
  }
}
