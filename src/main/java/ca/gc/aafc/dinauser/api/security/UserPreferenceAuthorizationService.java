package ca.gc.aafc.dinauser.api.security;

import ca.gc.aafc.dina.security.PermissionAuthorizationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * UserPreference can only be altered by the user or an admin.
 * To check if the user is trying to change its own record we compare the internalIdentifier (from Keycloak) to the
 * UserPreference userId.
 */
@Service
public class UserPreferenceAuthorizationService extends PermissionAuthorizationService {

  @PreAuthorize("@currentUser.internalIdentifier == T(java.util.Objects).toString(#entity.userId) " +
          "|| hasDinaRole(@currentUser, 'DINA_ADMIN')")
  @Override
  public void authorizeCreate(Object entity) {
  }

  @Override
  public void authorizeRead(Object entity) {
  }

  @PreAuthorize("@currentUser.internalIdentifier == T(java.util.Objects).toString(#entity.userId) " +
          "|| hasDinaRole(@currentUser, 'DINA_ADMIN')")
  @Override
  public void authorizeUpdate(Object entity) {
  }

  @PreAuthorize("@currentUser.internalIdentifier == T(java.util.Objects).toString(#entity.userId) " +
          "|| hasDinaRole(@currentUser, 'DINA_ADMIN')")
  @Override
  public void authorizeDelete(Object entity) {
  }

  @Override
  public String getName() {
    return "UserPreferenceAuthorizationService";
  }
}
