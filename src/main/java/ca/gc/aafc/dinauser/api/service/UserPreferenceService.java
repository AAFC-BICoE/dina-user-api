package ca.gc.aafc.dinauser.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.dinauser.api.crudit.UserPreference;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import io.crnk.core.exception.BadRequestException;
import lombok.NonNull;

import java.util.UUID;

public class UserPreferenceService extends DefaultDinaService<UserPreference> {

  private final DinaService<DinaUserDto> userService;

  public UserPreferenceService(@NonNull BaseDAO baseDAO, @NonNull DinaService<DinaUserDto> userService) {
    super(baseDAO);
    this.userService = userService;
  }

  @Override
  protected void preCreate(UserPreference entity) {
    if (userDoesNotExist(entity.getUserId())) {
      throw new BadRequestException("User with Id " + entity.getUserId() + " does not exist.");
    }
  }

  private boolean userDoesNotExist(UUID userId) {
    return !userService.exists(DinaUserDto.class, userId.toString());
  }
}
