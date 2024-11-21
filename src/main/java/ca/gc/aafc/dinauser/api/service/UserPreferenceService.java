package ca.gc.aafc.dinauser.api.service;

import org.springframework.stereotype.Component;
import org.springframework.validation.SmartValidator;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.dinauser.api.entity.UserPreference;

import io.crnk.core.exception.BadRequestException;
import java.util.UUID;
import lombok.NonNull;

@Component
public class UserPreferenceService extends DefaultDinaService<UserPreference> {

  private final DinaUserService userService;

  public UserPreferenceService(
    @NonNull BaseDAO baseDAO,
    @NonNull DinaUserService userService,
    @NonNull SmartValidator smartValidator
  ) {
    super(baseDAO, smartValidator);
    this.userService = userService;
  }

  @Override
  protected void preCreate(UserPreference entity) {
    entity.setUuid(UUID.randomUUID());
  }

  @Override
  public void validateBusinessRules(UserPreference entity) {
    // Ensure referential integrity
    validateUserExists(entity.getUserId());
  }

  private void validateUserExists(@NonNull UUID id) {
    if (!userService.exists(id.toString())) {
      throw new BadRequestException("User with Id " + id + " does not exist.");
    }
  }

}
