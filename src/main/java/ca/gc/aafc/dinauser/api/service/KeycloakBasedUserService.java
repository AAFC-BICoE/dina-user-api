package ca.gc.aafc.dinauser.api.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.security.DinaRole;
import ca.gc.aafc.dina.security.KeycloakClaimParser;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Service
@ConditionalOnProperty(value = "keycloak.enabled", matchIfMissing = true)
@Log4j2
public class KeycloakBasedUserService implements DinaUserService {

  private static final String LOCATION_HTTP_HEADER_KEY = "Location";

  private static final Set<String> DINA_ROLES = Arrays.stream(DinaRole.values())
    .map(DinaRole::getKeycloakRoleName).collect(Collectors.toSet());

  private static final Set<String> ADMIN_BASED_ROLES = DinaRole.adminBasedRoles().stream()
    .map(DinaRole::getKeycloakRoleName)
    .collect(Collectors.toSet());
  
  @Autowired
  private KeycloakClientService keycloakClientService;

  @Autowired
  private Keycloak keycloakClient;

  private RealmResource getRealmResource() {
    return keycloakClient.realm(keycloakClientService.getRealm());
  }

  private UsersResource getUsersResource() {
    return getRealmResource().users();
  }

  private List<UserRepresentation> retrieveGroupMembers(String groupId) {
    return getRealmResource().groups().group(groupId).members();
  }

  private UserRepresentation convertToRepresentation(final DinaUserDto user) {
    if (user == null) {
      log.error("cannot convert null user");
      return null;
    }
    final UserRepresentation rep = new UserRepresentation();

    rep.setUsername(user.getUsername());
    rep.setId(user.getInternalId());
    rep.setFirstName(user.getFirstName());
    rep.setLastName(user.getLastName());
    rep.setEmail(user.getEmailAddress());
    rep.singleAttribute(AGENT_ID_ATTR_KEY, user.getAgentId());

    return rep;
  }

  public static DinaUserDto convertFromRepresentation(final UserRepresentation rawUser) {
    if (rawUser == null) {
      log.error("cannot convert null user");
      return null;
    }

    final DinaUserDto user = new DinaUserDto();

    user.setUsername(rawUser.getUsername());
    user.setInternalId(rawUser.getId());
    user.setFirstName(rawUser.getFirstName());
    user.setLastName(rawUser.getLastName());
    user.setEmailAddress(rawUser.getEmail());
    user.setAgentId(DinaUserService.getAgentId(rawUser.getAttributes()));

    log.debug("built basic user DTO for user {}", user.getUsername());

    return user;
  }

  private DinaUserDto convertFromResource(final UserResource rawUser) {
    if (rawUser == null) {
      log.error("cannot convert null user");
      return null;
    }

    final DinaUserDto user = convertFromRepresentation(rawUser.toRepresentation());

    if (user != null) {
      // Populate group based roles.
      user.setRolesPerGroup(parseRolesPerGroup(rawUser.groups()
        .stream()
        .map(GroupRepresentation::getPath)
        .collect(Collectors.toList())));

      // Admin roles are non-group based, based on the supported admin based roles, we can determine
      // if the user has admin roles.
      user.setAdminRoles(rawUser.roles().realmLevel().listEffective().stream()
        .map(RoleRepresentation::getName)
        .filter(ADMIN_BASED_ROLES::contains)
        .collect(Collectors.toSet()));

      log.debug("filled in all attributes for user {}", user.getUsername());
    }

    return user;
  }

  /**
   * Helper method to parse Dina roles per group using a given list of keycloak group paths.
   *
   * @param groupPaths - keycloak group paths to parse
   * @return - Dina roles per group
   */
  private static Map<String, Set<String>> parseRolesPerGroup(@NonNull List<String> groupPaths) {
    return KeycloakClaimParser.parseGroupClaims(groupPaths).entrySet().stream().collect(Collectors.toMap(
      Map.Entry::getKey,
      entry -> entry.getValue().stream().map(DinaRole::getKeycloakRoleName).collect(Collectors.toSet())));
  }

  public static Map<String, Set<DinaRole>> parseDinaRolesPerGroup(@NonNull List<GroupRepresentation> groups) {
    return KeycloakClaimParser.parseGroupClaims(groups
      .stream()
      .map(GroupRepresentation::getPath)
      .collect(Collectors.toList()));
  }

//  public static Set<DinaRole> parseDinaRolesForGroup(@NonNull GroupRepresentation group) {
//    return KeycloakClaimParser.parseGroupClaims(List.of(group.getPath()));
//  }

  private void updateRoles(final DinaUserDto user, final UserResource userRes) {
    final RoleScopeResource userRolesRes = userRes.roles().realmLevel();

    final Set<String> desiredRoleNames = user.getRolesPerGroup().values()
      .stream()
      .flatMap(Collection::stream)
      .collect(Collectors.toUnmodifiableSet());
    log.debug("desired roles: {}", desiredRoleNames);

    final List<RoleRepresentation> currentRoles = userRolesRes.listEffective();
    log.debug("existing roles: {}", currentRoles);
    final List<RoleRepresentation> availableRoles = userRolesRes.listAvailable();

    // we only allow to add roles that are available and dina related
    List<RoleRepresentation> rolesToAdd = availableRoles.stream()
      .filter(r -> DINA_ROLES.contains(r.getName()))
      .filter(r -> desiredRoleNames.contains(r.getName()))
      .collect(Collectors.toList());
    log.debug("rolesToAdd: {}", rolesToAdd);

    // we only allow to remove roles that are dina related
    List<RoleRepresentation> rolesToRemove = currentRoles.stream()
      .filter(r -> DINA_ROLES.contains(r.getName()))
      .filter(r -> !desiredRoleNames.contains(r.getName()))
      .collect(Collectors.toList());
    log.debug("rolesToRemove: {}", rolesToRemove);

    final Set<String> allValidRoleNames =
      Stream.concat(currentRoles.stream(), availableRoles.stream())
        .map(RoleRepresentation::getName)
        .collect(Collectors.toSet());

    final List<String> invalidRoles = desiredRoleNames.stream()
      .filter(r -> !allValidRoleNames.contains(r))
      .collect(Collectors.toList());

    if (!invalidRoles.isEmpty()) {
      log.warn("skipped invalid roles: {}", invalidRoles);
    }

    userRolesRes.add(rolesToAdd);
    userRolesRes.remove(rolesToRemove);

  }

  private void updateGroups(final DinaUserDto user, final UserResource userRes) {

    final List<GroupRepresentation> currentGroups = userRes.groups();
    final Set<String> currentGroupIds = currentGroups.stream()
      .map(GroupRepresentation::getId)
      .collect(Collectors.toSet());
    log.debug("current group ids: {}", currentGroupIds);

    Set<String> groups = generateKeycloakGroupPaths(user.getRolesPerGroup());
    final Set<String> desiredGroupIds = groups.stream()
      .distinct()
      .map(p -> {
        try {
          return getRealmResource().getGroupByPath(p).getId();
        } catch (NotFoundException e) {
          log.debug("Invalid group: {}", p);
          return null;
        }
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());
    log.debug("desired group ids: {}", desiredGroupIds);

    final Set<String> groupsToAdd = desiredGroupIds.stream()
      .filter(g -> !currentGroupIds.contains(g))
      .collect(Collectors.toSet());
    log.debug("to add: {}", groupsToAdd);

    final Set<String> groupsToRemove = currentGroupIds.stream()
      .filter(g -> !desiredGroupIds.contains(g))
      .collect(Collectors.toSet());
    log.debug("to remove: {}", groupsToRemove);

    for (final String groupId : groupsToAdd) {
      userRes.joinGroup(groupId);
    }

    for (final String groupId : groupsToRemove) {
      userRes.leaveGroup(groupId);
    }

  }

  /**
   * Helper method to generate keycloak compatible group paths by combining a map of roles per group.
   *
   * @param rolesPerGroup - map of roles per group
   * @return - a set of keycloak group paths
   */
  private static Set<String> generateKeycloakGroupPaths(@NonNull Map<String, Set<String>> rolesPerGroup) {
    return rolesPerGroup.entrySet().stream()
      .map(e -> e.getValue().stream().map(role -> e.getKey() + "/" + role).collect(Collectors.toSet()))
      .flatMap(Collection::stream).collect(Collectors.toSet());
  }

  public Integer getUserCount() {
    log.debug("getting user count");
    return getUsersResource().count();
  }

  public List<DinaUserDto> getUsers(Set<String> groups, java.util.function.Predicate<DinaUserDto> predicate, Comparator<DinaUserDto> sortComparator) {
    final RealmResource realmResource = getRealmResource();

    Set<DinaUserDto> uniqueUsers = new HashSet<>();
    for (String group : groups) {
      GroupRepresentation grp = realmResource.getGroupByPath(group);
      Set<String> groupsId =
        grp.getSubGroups().stream().map(GroupRepresentation::getId).collect(Collectors.toSet());

      for (String groupId : groupsId) {
        uniqueUsers.addAll(
          retrieveGroupMembers(groupId)
            .stream()
            .map(u -> convertFromResource(realmResource.users().get(u.getId()))).toList());
      }
    }

    return applyToStream(uniqueUsers.stream(), predicate, sortComparator);
  }

  @Override
  public List<DinaUserDto> getAllUsers(java.util.function.Predicate<DinaUserDto> predicate, Comparator<DinaUserDto> sortComparator) {
    return getUsers(null, null, predicate, sortComparator);
  }

  @Override
  public List<DinaUserDto> getUsers(final Integer firstResult, final Integer maxResults,
                                    java.util.function.Predicate<DinaUserDto> predicate, Comparator<DinaUserDto> sortComparator) {
    log.debug("getting raw user list from {} ({} max)", firstResult, maxResults);
    final List<UserRepresentation> rawUsers = getUsersResource().list(firstResult, maxResults);

    log.debug("converting users");
    Stream<DinaUserDto> userStream = rawUsers
      .stream()
      .map(u -> convertFromResource(getUsersResource().get(u.getId())));

    return applyToStream(userStream, predicate, sortComparator);
  }

  private static List<DinaUserDto> applyToStream(Stream<DinaUserDto> stream, java.util.function.Predicate<DinaUserDto> predicate, Comparator<DinaUserDto> sortComparator) {
    Stream<DinaUserDto> userStream = stream;
    if (predicate != null) {
      userStream = userStream.filter(predicate);
    }

    if (sortComparator != null) {
      userStream = userStream.sorted(sortComparator);
    }

    return userStream.collect(Collectors.toList());
  }

  public DinaUserDto getUser(final String id) {
    log.debug("getting user with id {}", id);

    try {
      final UserResource rawUser = getUsersResource().get(id);

      if (rawUser != null) {
        log.debug("found user {}", rawUser.toRepresentation().getUsername());
        return convertFromResource(rawUser);
      } else {
        log.debug("user not found");
        return null;
      }
    } catch (NotFoundException nfEx) {
      return null;
    }
  }

  private void updateGroupsAndRoles(final DinaUserDto user, final UserResource userRes) {
    updateGroups(user, userRes);
    // Note: updateRoles MUST come after groups, because the user's effective roles can be affected by group membership
    updateRoles(user, userRes);
  }

  public DinaUserDto createUser(final DinaUserDto user) {
    //TODO validation, duplicate checks
    //TODO add credentials (temp password)
    final UserRepresentation rep = convertToRepresentation(user);
    final Response response = getUsersResource().create(rep);

    log.debug("response status: {}", response.getStatus());

    if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
      final String newUserUrl = response.getHeaderString(LOCATION_HTTP_HEADER_KEY);
      final Matcher m = UUID_REGEX.matcher(newUserUrl);

      if (m.find()) {
        final String createdUserId = m.group();
        final UserResource newUserRes = getUsersResource().get(createdUserId);

        updateGroupsAndRoles(user, newUserRes);

        final DinaUserDto createdUser = getUser(createdUserId);

        log.debug("returning new user {}", createdUser.getUsername());

        return createdUser;
      }

    }
    throw new IllegalStateException();
  }

  @Override
  public DinaUserDto update(DinaUserDto user) {
    return updateUser(user);
  }

  public DinaUserDto updateUser(final DinaUserDto user) {
    final UserRepresentation rep = convertToRepresentation(user);
    if (rep != null) {
      final UserResource existingUserRes = getUsersResource().get(rep.getId());
      updateGroupsAndRoles(user, existingUserRes);
      existingUserRes.update(rep);
      log.debug("returning updated user {}", user.getUsername());
      return getUser(rep.getId());
    }
    return user;
  }

  public void deleteUser(final String id) {
    final UserResource res = getUsersResource().get(id);
    res.remove();
  }

  @Override
  public DinaUserDto create(DinaUserDto entity) {
    DinaUserDto user = this.createUser(entity);
    // Small flaw in dina repo, need to set id on incoming entity
    entity.setInternalId(user.getInternalId());
    return user;
  }

  public DinaUserDto findOne(Object naturalId) {
    return getUser(naturalId.toString());
  }

  public boolean exists(Object naturalId) {
    return this.getUsersResource().count("id:" + naturalId) == 1;
  }
}

