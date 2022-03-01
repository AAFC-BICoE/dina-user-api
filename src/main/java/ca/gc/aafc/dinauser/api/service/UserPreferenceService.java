package ca.gc.aafc.dinauser.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.dinauser.api.entity.UserPreference;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import io.crnk.core.exception.BadRequestException;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.SmartValidator;

import java.util.UUID;

@Component
public class UserPreferenceService extends DefaultDinaService<UserPreference> {

  private final DinaService<DinaUserDto> userService;

  public UserPreferenceService(
    @NonNull BaseDAO baseDAO,
    @NonNull DinaService<DinaUserDto> userService,
    @NonNull SmartValidator smartValidator
  ) {
    super(baseDAO, smartValidator);
    this.userService = userService;
  }

  @Override
  protected void preCreate(UserPreference entity) {
    // Ensure referential integrity
    validateUserExists(entity.getUserId());

    entity.setUuid(UUID.randomUUID());
  }

  @Override
  protected void preUpdate(UserPreference entity) {
    // Ensure referential integrity
    validateUserExists(entity.getUserId());
  }

  private void validateUserExists(@NonNull UUID id) {
    if (!userService.exists(DinaUserDto.class, id.toString())) {
      throw new BadRequestException("User with Id " + id + " does not exist.");
    }
  }

}
