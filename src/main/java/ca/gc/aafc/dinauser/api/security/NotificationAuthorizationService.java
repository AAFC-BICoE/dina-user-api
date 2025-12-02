package ca.gc.aafc.dinauser.api.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.security.auth.PermissionAuthorizationService;

/**
 * Notification can only be altered by the user or an admin.
 * To check if the user is trying to change its own record we compare the internalIdentifier (from Keycloak) to the
 * Notification userId.
 */
@Service
public class NotificationAuthorizationService extends PermissionAuthorizationService {

  @PreAuthorize("@currentUser.internalIdentifier == T(java.util.Objects).toString(#entity.userIdentifier) " +
          "|| hasAdminRole(@currentUser, 'DINA_ADMIN')")
  @Override
  public void authorizeCreate(Object entity) {
  }

  @PreAuthorize("@currentUser.internalIdentifier == T(java.util.Objects).toString(#entity.userIdentifier) " +
    "|| hasAdminRole(@currentUser, 'DINA_ADMIN')")
  @Override
  public void authorizeRead(Object entity) {
  }

  @PreAuthorize("@currentUser.internalIdentifier == T(java.util.Objects).toString(#entity.userIdentifier) " +
          "|| hasAdminRole(@currentUser, 'DINA_ADMIN')")
  @Override
  public void authorizeUpdate(Object entity) {
  }

  @PreAuthorize("@currentUser.internalIdentifier == T(java.util.Objects).toString(#entity.userIdentifier) " +
          "|| hasAdminRole(@currentUser, 'DINA_ADMIN')")
  @Override
  public void authorizeDelete(Object entity) {
  }

  @Override
  public String getName() {
    return "NotificationAuthorizationService";
  }
}
