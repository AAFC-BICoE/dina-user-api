package ca.gc.aafc.dinauser.api.service;

import java.util.UUID;
import lombok.NonNull;

import org.springframework.stereotype.Service;
import org.springframework.validation.SmartValidator;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.dinauser.api.entity.Notification;
import ca.gc.aafc.dinauser.api.validation.NotificationValidator;

@Service
public class NotificationService extends DefaultDinaService<Notification> {

  private final NotificationValidator notificationValidator;

  public NotificationService(@NonNull BaseDAO baseDAO,
                             NotificationValidator notificationValidator,
                             @NonNull SmartValidator validator) {
    super(baseDAO, validator);
    this.notificationValidator = notificationValidator;
  }

  @Override
  protected void preCreate(Notification entity) {
    entity.setUuid(UUID.randomUUID());
  }

  @Override
  public void validateBusinessRules(Notification entity) {
    applyBusinessRule(entity, notificationValidator);
  }

}
