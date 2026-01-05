package ca.gc.aafc.dinauser.api.validation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import ca.gc.aafc.dina.validation.DinaBaseValidator;
import ca.gc.aafc.dinauser.api.entity.Notification;

import java.util.HashMap;
import java.util.Map;
import liquibase.repackaged.org.apache.commons.text.StringSubstitutor;

@Component
public class NotificationValidator extends DinaBaseValidator<Notification> {

  public NotificationValidator(MessageSource messageSource) {
    super(Notification.class, messageSource);
  }

  @Override
  public void validateTarget(Notification target, Errors errors) {
    if (StringUtils.isBlank(target.getMessage())) {
      return;
    }

    // Extract string values from MessageParam records. We only consider the first record since
    // the validation is only about making sure we have at least a value for all params in the message
    Map<String, String> paramValues = new HashMap<>();
    if (target.getMessageParams() != null) {
      target.getMessageParams().forEach((key, param) -> {
        if (CollectionUtils.isNotEmpty(param)) {
          paramValues.put(key, param.getFirst().value());
        }
      });
    }

    try {
      StringSubstitutor substitutor = new StringSubstitutor(paramValues);
      substitutor.setEnableUndefinedVariableException(true);
      substitutor.replace(target.getMessage()); // This will throw if variables are missing
    } catch (IllegalArgumentException e) {
      errors.rejectValue("message", "Invalid messageParams: " + e.getMessage());
    }
  }
}
