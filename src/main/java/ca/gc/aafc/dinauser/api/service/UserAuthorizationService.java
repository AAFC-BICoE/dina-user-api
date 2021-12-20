package ca.gc.aafc.dinauser.api.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.security.DinaRole;
import ca.gc.aafc.dina.security.PermissionAuthorizationService;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;

import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ForbiddenException;

@Service
public class UserAuthorizationService extends PermissionAuthorizationService {

  private static final Pattern NON_ALPHA = Pattern.compile("[^A-Za-z]");

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
  public void authorizeUpdate(Object entity) {
    handle(entity, (roles, highestRole) -> roles.forEach(role -> {
      if (role.isHigherOrEqualThan(highestRole)) {
        throw new ForbiddenException("You cannot update a User with role: " + role);
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
    if (isUserLessThenCollectionManager(authenticatedUser) &&
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

  public static boolean isUserLessThenCollectionManager(DinaAuthenticatedUser user) {
    return !findHighestRole(user).isHigherOrEqualThan(DinaRole.COLLECTION_MANAGER);
  }

  private static boolean isNotSameUser(DinaUserDto first, DinaAuthenticatedUser second) {
    return !second.getInternalIdentifier().equalsIgnoreCase(first.getInternalId());
  }

  private static DinaRole findHighestRole(DinaAuthenticatedUser authenticatedUser) {
    // Get a stream of all the users unique roles, and find the highest role.
    return authenticatedUser.getRolesPerGroup().values()
      .stream()
      .flatMap(Collection::stream)
      .distinct()
      .max((role1, role2) -> role1.isHigherThan(role2) ? 1 : -1)
      .orElseThrow(() -> new ForbiddenException("You do not have any roles"));
  }

  private static DinaRole fromString(String roleString) {
    return Arrays.stream(DinaRole.values())
      .filter(dinaRole -> dinaRole.name()
        .equalsIgnoreCase(NON_ALPHA.matcher(roleString).replaceAll("_")))
      .findAny()
      .orElseThrow(() -> new BadRequestException(roleString + " is not a valid DinaRole"));
  }
}
