package ca.gc.aafc.dinauser.api.service;

import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.security.DinaRole;
import ca.gc.aafc.dina.service.DinaAuthorizationService;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ForbiddenException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class UserAuthorizationService implements DinaAuthorizationService {

  private static final Map<DinaRole, Integer> ROLE_WEIGHT_MAP = Map.of(
    DinaRole.DINA_ADMIN, 4,
    DinaRole.COLLECTION_MANAGER, 3,
    DinaRole.STAFF, 2,
    DinaRole.STUDENT, 1
  );
  private static final Pattern NON_ALPHA = Pattern.compile("[^A-Za-z]");

  @Inject
  DinaAuthenticatedUser authenticatedUser;

  @Override
  public void authorizeCreate(Object entity) {
    handle(entity, (roles, highestRole) -> roles.forEach(role -> {
      if (ROLE_WEIGHT_MAP.get(role) >= highestRole) {
        throw new ForbiddenException("You cannot create a User with role: " + role);
      }
    }));
  }

  @Override
  public void authorizeUpdate(Object entity) {
    handle(entity, (roles, highestRole) -> roles.forEach(role -> {
      if (ROLE_WEIGHT_MAP.get(role) >= highestRole) {
        throw new ForbiddenException("You cannot update a User with role: " + role);
      }
    }));
  }

  @Override
  public void authorizeDelete(Object entity) {
    handle(entity, (roles, highestRole) -> roles.forEach(role -> {
      if (ROLE_WEIGHT_MAP.get(role) >= highestRole) {
        throw new ForbiddenException("You cannot delete a User with role: " + role);
      }
    }));
  }

  private void handle(Object entity, BiConsumer<Stream<DinaRole>, Integer> consumer) {
    if (entity instanceof DinaUserDto) {
      Integer highestRole = findHighestRole(authenticatedUser);
      if (highestRole < ROLE_WEIGHT_MAP.get(DinaRole.DINA_ADMIN)) {
        DinaUserDto obj = (DinaUserDto) entity;
        consumer.accept(
          obj.getRoles().stream().map(UserAuthorizationService::fromString),
          highestRole);
      }
    } else {
      throw new IllegalArgumentException("This service can only handle DinaUserDto's");
    }
  }

  private static Integer findHighestRole(DinaAuthenticatedUser authenticatedUser) {
    return authenticatedUser.getRolesPerGroup()
      .values()
      .stream()
      .flatMap(Collection::stream)
      .map(ROLE_WEIGHT_MAP::get)
      .distinct()
      .reduce(Math::max)
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
