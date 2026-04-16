package ca.gc.aafc.dinauser.api.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.TransactionTestingHelper;
import ca.gc.aafc.dinauser.api.DinaUserModuleApiLauncher;
import ca.gc.aafc.dinauser.api.config.UserModuleTestConfiguration;
import ca.gc.aafc.dinauser.api.entity.Notification;

import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = {UserModuleTestConfiguration.class, DinaUserModuleApiLauncher.class})
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@ContextConfiguration(initializers = { PostgresTestContainerInitializer.class })
public class ExpiredNotificationServiceIT {

  @Inject
  private NotificationService notificationService;

  @Inject
  private ExpiredNotificationService expiredNotificationService;

  @Inject
  private TransactionTestingHelper transactionTestingHelper;

  public static final String INTERVAL_2_WEEKS = "UPDATE object_upload SET created_on = created_on - interval '2 weeks'";

  @Test
  @Transactional
  public void onExpiredNotification_removeExpiredNotification() {
    Notification notification = Notification.builder()
      .expiresOn(LocalDateTime.now().minusWeeks(2).atZone(ZoneId.systemDefault())
        .toOffsetDateTime())
      .type("abc")
      .title("my title")
      .status(Notification.Status.NEW)
      .build();

    Notification notificationCreated = notificationService.create(notification);

    UUID uuid = notificationCreated.getUuid();
    expiredNotificationService.removeExpiredNotification();

    Notification notificationFound = notificationService.findOne(uuid, Notification.class);
    assertNull(notificationFound);
  }
}
