package ca.gc.aafc.dinauser.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.dinauser.api.entity.UserPreference;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import io.crnk.core.exception.BadRequestException;
import lombok.NonNull;

public class UserPreferenceService extends DefaultDinaService<UserPreference> {

  private final DinaService<DinaUserDto> userService;

  public UserPreferenceService(@NonNull BaseDAO baseDAO, @NonNull DinaService<DinaUserDto> userService) {
    super(baseDAO);
    this.userService = userService;
  }

  @Override
  protected void preCreate(UserPreference entity) {
    // Ensure referential integrity
    validateUserExists(entity.getUserId());
  }

  @Override
  protected void preUpdate(UserPreference entity) {
    // Ensure referential integrity
    validateUserExists(entity.getUserId());
  }

  private void validateUserExists(@NonNull String id) {
    if (!userService.exists(DinaUserDto.class, id)) {
      throw new BadRequestException("User with Id " + id + " does not exist.");
    }
  }

}
