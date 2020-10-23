package ca.gc.aafc.dinauser.api.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;

import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.service.KeycloakClientService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class DinaUserService {

  private static final String AGENT_ID_ATTR_KEY = "agentId";

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

  private String getAgentId(final UserRepresentation userRep) {
    final Map<String, List<String>> attrs = userRep.getAttributes();
    if (attrs == null) {
      log.warn("User '{}' has no attribute map", userRep.getUsername());
      return null;
    }

    final List<String> agentIds = attrs.get(AGENT_ID_ATTR_KEY);
    if (agentIds == null || agentIds.isEmpty()) {
      log.warn("User '{}' has no agentId", userRep.getUsername());
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

    return user;
  }

  private DinaUserDto convertFromResource(final UserResource rawUser) {
    if (rawUser == null) {
      log.error("cannot convert null user");
      return null;
    }

    final DinaUserDto user = convertFromRepresentation(rawUser.toRepresentation());

    //TODO fill in other fields
//    List<CredentialRepresentation> credentials = rawUser.credentials();
//    List<String> userStorageCredTypes = rawUser.getConfiguredUserStorageCredentialTypes();
//    List<Map<String, Object>> consents = rawUser.getConsents();
//    List<FederatedIdentityRepresentation> fedIdentity = rawUser.getFederatedIdentity();
//    List<UserSessionRepresentation> sessions = rawUser.getUserSessions();
    List<GroupRepresentation> groups = rawUser.groups();
    user.getGroups().addAll(groups
        .stream()
        .map(g -> g.getPath())
        .collect(Collectors.toList()));

    RoleMappingResource roleMappingResource = rawUser.roles();

    RoleScopeResource realmLevelRoles = roleMappingResource.realmLevel();
    List<RoleRepresentation> effectiveRoles = realmLevelRoles.listEffective();

    // available roles = not assigned; maybe useful
    //List<RoleRepresentation> availableRoles = realmLevelRoles.listAvailable();

    user.getRoles().addAll(effectiveRoles
        .stream()
        .map(r -> r.getName())
        .collect(Collectors.toList()));

    log.info("got a bunch of stuff");

    return user;
  }

  private void updateGroups(final DinaUserDto user, final UserResource userRes) {

    final List<GroupRepresentation> currentGroups = userRes.groups();
    final Set<String> currentGroupIds = currentGroups.stream()
        .map(g -> g.getId())
        .distinct()
        .collect(Collectors.toSet());
    log.debug("current group ids: {}", currentGroupIds);

    final Set<String> desiredGroupIds = user.getGroups().stream()
        .distinct()
        .map(p -> getRealmResource().getGroupByPath(p).getId())
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

  public Integer getUserCount() {
    log.debug("getting user count");
    return getUsersResource().count();
  }

  public List<DinaUserDto> getUsers() {
    return getUsers(null, null);
  }

  public List<DinaUserDto> getUsers(final Integer firstResult, final Integer maxResults) {
    log.debug("getting raw user list from {} ({} max)", firstResult, maxResults);
    final List<UserRepresentation> rawUsers = getUsersResource().list(firstResult, maxResults);

    log.debug("converting users");
    final List<DinaUserDto> cookedUsers = rawUsers
        .stream()
        .map(u -> convertFromRepresentation(u))
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

  public void createUser(final DinaUserDto user) {
    //TODO validation, duplicate checks
    //TODO handle roles, groups
    //TODO add credentials (temp password)
    final UserRepresentation rep = convertToRepresentation(user);
    final Response response = getUsersResource().create(rep);
    //TODO process response
    log.debug("response status: {}", response.getStatus());
  }

  public void updateUser(final DinaUserDto user) {
    final UserRepresentation rep = convertToRepresentation(user);
    final UserResource existingUserRes = getUsersResource().get(rep.getId());

    updateGroups(user, existingUserRes);

    existingUserRes.update(rep);
  }

  public void deleteUser(final String id) {
    final UserResource res = getUsersResource().get(id);
    res.remove();
  }

}