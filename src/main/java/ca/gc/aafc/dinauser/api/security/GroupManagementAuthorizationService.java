package ca.gc.aafc.dinauser.api.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.security.auth.PermissionAuthorizationService;

/**
 * Group management is to manage the group themselves, not the membership of the group.
 */
@Service
public class GroupManagementAuthorizationService extends PermissionAuthorizationService {

  @Override
  @PreAuthorize("hasAdminRole(@currentUser, 'DINA_ADMIN')")
  public void authorizeCreate(Object entity) {

  }

  @Override
  public void authorizeRead(Object entity) {

  }

  @PreAuthorize("hasAdminRole(@currentUser, 'DINA_ADMIN')")
  @Override
  public void authorizeUpdate(Object entity) {

  }

  @Override
  @PreAuthorize("denyAll()")
  public void authorizeDelete(Object entity) {

  }

  @Override
  public String getName() {
    return "GroupAuthorizationService";
  }
}
