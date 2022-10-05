package ca.gc.aafc.dinauser.api.service;

import ca.gc.aafc.dina.jpa.PredicateSupplier;
import ca.gc.aafc.dina.security.DinaRole;
import ca.gc.aafc.dina.security.KeycloakClaimParser;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.exception.CrnkMappableException;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.groups.Default;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Log4j2
public class DinaUserService implements DinaService<DinaUserDto> {

  private static final String AGENT_ID_ATTR_KEY = "agentId";
  private static final String LOCATION_HTTP_HEADER_KEY = "Location";

  private static final Set<String> DINA_ROLES = Arrays.stream(DinaRole.values())
      .map(DinaRole::getKeycloakRoleName).collect(Collectors.toSet());

  private static final Pattern UUID_REGEX = Pattern.compile(
    "[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}");

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

  private String getAgentId(final UserRepresentation userRep) {
    Map<String, List<String>> attrs = userRep.getAttributes();
    if (attrs == null) {
      log.warn("User '{}' has no attribute map", userRep.getUsername());
      return null;
    }

    List<String> agentIds = attrs.get(AGENT_ID_ATTR_KEY);
    if (agentIds == null || agentIds.isEmpty()) {
      log.warn("User '{}' has no agentId", userRep.getUsername());
      return null;
    }

    return agentIds.get(0);
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

  private DinaUserDto convertFromRepresentation(final UserRepresentation rawUser) {
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
    user.setAgentId(getAgentId(rawUser));

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
      user.setRolesPerGroup(parseRolesPerGroup(rawUser.groups()
        .stream()
        .map(GroupRepresentation::getPath)
        .collect(Collectors.toList())));
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

    if (invalidRoles.size() > 0) {
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

  public List<DinaUserDto> getUsers(Set<String> groups) {
    final RealmResource realmResource = getRealmResource();

    Set<DinaUserDto> uniqueUsers = new HashSet<>();
    for(String group : groups) {
      GroupRepresentation grp = realmResource.getGroupByPath(group);
      Set<String> groupsId = grp.getSubGroups().stream().map(GroupRepresentation::getId).collect(Collectors.toSet());

      for(String groupId : groupsId) {
        uniqueUsers.addAll(
                retrieveGroupMembers(groupId)
                        .stream()
                        .map(u -> convertFromResource(realmResource.users().get(u.getId()))).toList());
      }
    }

    return List.copyOf(uniqueUsers);
  }

  public List<DinaUserDto> getAllUsers() {
    return getUsers(null, null);
  }

  public List<DinaUserDto> getUsers(final Integer firstResult, final Integer maxResults) {
    log.debug("getting raw user list from {} ({} max)", firstResult, maxResults);
    final List<UserRepresentation> rawUsers = getUsersResource().list(firstResult, maxResults);

    log.debug("converting users");
    final List<DinaUserDto> cookedUsers = rawUsers
      .stream()
      .map(u -> convertFromResource(getUsersResource().get(u.getId())))
      .collect(Collectors.toList());

    log.debug("done converting users; returning");
    return cookedUsers;
  }

  public DinaUserDto getUser(final String id) {
    log.debug("getting user with id {}", id);
    final UserResource rawUser = getUsersResource().get(id);

    if (rawUser != null) {
      log.debug("found user {}", rawUser.toRepresentation().getUsername());
      return convertFromResource(rawUser);
    } else {
      log.debug("user not found");
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

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
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

    //TODO more appropriate exception?

    final ErrorData errorData = ErrorData.builder()
      .setStatus(response.getStatusInfo().toString())
      .build();

    throw new CrnkMappableException(response.getStatus(), errorData) {
      private static final long serialVersionUID = -4639135098679358400L;
    };

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

  @Override
  public DinaUserDto update(DinaUserDto entity) {
    return this.updateUser(entity);
  }

  @Override
  public void delete(DinaUserDto entity) {
    this.deleteUser(entity.getInternalId());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T findOne(Object naturalId, Class<T> entityClass) {
    validateFindClass(entityClass);
    return (T) this.getUser(naturalId.toString());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getReferenceByNaturalId(Class<T> entityClass, Object naturalId) {
    validateFindClass(entityClass);
    return (T) this.getUser(naturalId.toString());
  }

  @Override
  public <T> List<T> findAll(
    @NonNull Class<T> entityClass,
    @NonNull BiFunction<CriteriaBuilder, Root<T>, Predicate[]> where,
    BiFunction<CriteriaBuilder, Root<T>, List<Order>> orderBy,
    int startIndex,
    int maxResult
  ) {
    return this.findAll(
      entityClass,
      (criteriaBuilder, root, em) -> where.apply(criteriaBuilder, root),
      orderBy,
      startIndex,
      maxResult, Set.of(), Set.of());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> List<T> findAll(@NonNull Class<T> entityClass, @NonNull PredicateSupplier<T> where, BiFunction<CriteriaBuilder, Root<T>, List<Order>> orderBy,
                             int startIndex, int maxResult, @NonNull Set<String> includes, @NonNull Set<String> relationships) {
    validateFindClass(entityClass);
    return (List<T>) this.getAllUsers();
  }

  @Override
  public <T> Long getResourceCount(
    @NonNull Class<T> entityClass,
    @NonNull BiFunction<CriteriaBuilder, Root<T>, Predicate[]> predicateSupplier
  ) {
    return this.getResourceCount(
      entityClass,
      (criteriaBuilder, root, em) -> predicateSupplier.apply(criteriaBuilder, root));
  }

  /**
   * warning: predicateSupplier ignored
   * @param entityClass
   * @param predicateSupplier
   * @return
   */
  @Override
  public <T> Long getResourceCount(
    @NonNull Class<T> entityClass,
    @NonNull PredicateSupplier<T> predicateSupplier
  ) {
    validateFindClass(entityClass);
    return (long) this.getUserCount();
  }

  @Override
  public boolean exists(Class<?> entityClass, Object naturalId) {
    validateFindClass(entityClass);
    return this.getUsersResource().count("id:" + naturalId) == 1;
  }

  @Override
  public void validateConstraints(DinaUserDto entity, Class<? extends Default> validationGroup) {
    // no constraints for now.
  }

  @Override
  public void validateBusinessRules(DinaUserDto entity) {
    // no business rules for now.
  }

  private <T> void validateFindClass(Class<T> entityClass) {
    if (!(entityClass.equals(DinaUserDto.class))) {
      throw new IllegalArgumentException("This service can only find DinaUserDto's");
    }
  }
}
