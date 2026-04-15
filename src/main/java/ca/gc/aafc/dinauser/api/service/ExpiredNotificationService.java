package ca.gc.aafc.dinauser.api.service;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dinauser.api.entity.Notification;

@Service
@Log4j2
public class ExpiredNotificationService {

  public static final String EVERY_DAY_3_AM = "0 0 3 * * *";

  private final BaseDAO baseDAO;
  private final NotificationService notificationService;

  public ExpiredNotificationService(BaseDAO baseDAO,
                                    NotificationService notificationService) {
    this.baseDAO = baseDAO;
    this.notificationService = notificationService;
  }

  @Scheduled(cron = EVERY_DAY_3_AM)
  @Transactional
  public void removeExpiredNotification() {
    String hql = "FROM " + Notification.class.getCanonicalName() +
      " e WHERE e.expiredOn < current_timestamp()";
    List<Notification>
      expiredNotifications = baseDAO.findAllByQuery(Notification.class, hql, null, 0, 100);

    expiredNotifications.forEach(notificationService::delete);
  }
}
