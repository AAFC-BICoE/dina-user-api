package ca.gc.aafc.dinauser.api.security;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.security.DinaRole;
import ca.gc.aafc.dina.security.auth.PermissionAuthorizationService;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;

import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ForbiddenException;

@Service
public class UserAuthorizationService extends PermissionAuthorizationService {

  @Inject
  private DinaAuthenticatedUser authenticatedUser;

  @Override
  public void authorizeCreate(Object entity) {
    handle(entity, (roles, highestRole) -> roles.forEach(role -> {
      if (role.isHigherOrEqualThan(highestRole)) {
        throw new ForbiddenException("You cannot create a User with role: " + role);
      }
    }));
  }

  @Override
  public void authorizeRead(Object entity) {
    // nothing for now
  }

  @Override
  public void authorizeUpdate(Object entity) {
    handle(entity, (roles, highestRole) -> roles.forEach(role -> {

      if (entity instanceof DinaUserDto userDto) {
        // If the user is self, allow to preserve the highest role
        Function<DinaRole, Boolean> fct =
          isSameUser(userDto, authenticatedUser) ? role::isHigherThan :
            role::isHigherOrEqualThan;

        if (fct.apply(highestRole)) {
          throw new ForbiddenException("You cannot update a User with role: " + role);
        }
      }
    }));
  }

  @Override
  public void authorizeDelete(Object entity) {
    handle(entity, (roles, highestRole) -> roles.forEach(role -> {
      if (role.isHigherOrEqualThan(highestRole)) {
        throw new ForbiddenException("You cannot delete a User with role: " + role);
      }
    }));
  }

  @Override
  public String getName() {
    return "UserAuthorizationService";
  }

  public void authorizeFindOne(DinaUserDto resource) {
    if (!isSuperUserOrHigher(authenticatedUser) &&
        isNotSameUser(resource, authenticatedUser)) {
      throw new ForbiddenException("You can only view your own record");
    }
  }

  public void authorizeUpdateOnResource(Object resource) {
    handle(resource, (roles, highestRole) -> roles.forEach(dinaRole -> {
      if (dinaRole.isHigherOrEqualThan(highestRole)) {
        throw new ForbiddenException("You cannot update a User to have a role of: " + dinaRole);
      }
    }));
  }

  private void handle(Object entity, BiConsumer<Stream<DinaRole>, DinaRole> consumer) {
    if (entity instanceof DinaUserDto) {
      DinaRole highestRole = findHighestRole(authenticatedUser);
      if (!highestRole.isHigherThan(DinaRole.DINA_ADMIN)) {
        DinaUserDto obj = (DinaUserDto) entity;
        consumer.accept(
            obj.getRolesPerGroup()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .map(UserAuthorizationService::fromString),
            highestRole);
      }
    } else {
      throw new IllegalArgumentException("This service can only handle DinaUserDto's");
    }
  }

  /**
   * Checks if the provided user is a SUPER_USER or higher (in priority).
   * 
   * @param user
   * @return
   */
  public static boolean isSuperUserOrHigher(DinaAuthenticatedUser user) {
    return findHighestRole(user).isHigherOrEqualThan(DinaRole.SUPER_USER);
  }

  /**
   * Checks if the user represented by the {@link DinaUserDto} is the same as the authenticated user
   * based on the internalIdentifier value. If one of the values is blank this method returns false;
   * @param first
   * @param second
   * @return
   */
  private static boolean isSameUser(DinaUserDto first, DinaAuthenticatedUser second) {
    if (StringUtils.isBlank(first.getInternalId()) || StringUtils.isBlank(second.getInternalIdentifier())) {
      return false;
    }

    return second.getInternalIdentifier().equalsIgnoreCase(first.getInternalId());
  }

  private static boolean isNotSameUser(DinaUserDto first, DinaAuthenticatedUser second) {
    return !second.getInternalIdentifier().equalsIgnoreCase(first.getInternalId());
  }

  public static DinaRole findHighestRole(DinaAuthenticatedUser authenticatedUser) {
    // Get a stream of all the users unique roles, including admin roles, and find
    // the highest role.
    return Stream.concat(
        authenticatedUser.getRolesPerGroup().values().stream().flatMap(Collection::stream),
        authenticatedUser.getAdminRoles().stream())
        .distinct()
        .max((role1, role2) -> role1.isHigherThan(role2) ? 1 : -1)
        .orElseThrow(() -> new ForbiddenException("You do not have any roles"));
  }

  private static DinaRole fromString(String roleString) {
    return DinaRole.fromString(roleString)
        .orElseThrow(() -> new BadRequestException("Invalid DinaRole"));
  }
}
