package ca.gc.aafc.dinauser.api.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.security.DinaRole;
import ca.gc.aafc.dinauser.api.dto.DinaGroupDto;
import ca.gc.aafc.dinauser.api.dto.DinaGroupMembershipDto;
import ca.gc.aafc.dinauser.api.dto.DinaUserSummaryDto;

import static org.keycloak.admin.client.CreatedResponseUtil.getCreatedId;

@Service
@ConditionalOnProperty(value = "keycloak.enabled", matchIfMissing = true)
@Log4j2
public class KeycloakBasedGroupService implements DinaGroupService {

  private static final Pattern GROUP_NAME_REGEX = Pattern.compile("[a-z0-9][a-z0-9-]{1,61}[a-z0-9]");
  private static final Set<DinaRole>
    NON_GROUP_BASED_ROLES = EnumSet.of(DinaRole.DINA_ADMIN, DinaRole.READ_ONLY_ADMIN);

  private static final String LABEL_ATTR_KEY_PREFIX = "groupLabel-";
  private static final String GROUPS_CACHE_NAME = "groups";
  private static final int MAX_GROUP_MEMBER_PAGE_SIZE = 100;

  @Autowired
  private KeycloakClientService keycloakClientService;

  @Autowired
  private Keycloak keycloakClient;

  private RealmResource getRealmResource() {
    return keycloakClient.realm(keycloakClientService.getRealm());
  }

  private GroupsResource getGroupsResource() {
    return getRealmResource().groups();
  }

  private String getStringAttr(final Map<String, List<String>> attrs, final String key) {
    final List<String> list = attrs.get(key);
    if (list == null || list.isEmpty()) {
      log.warn("group has no {}", key);
      return null;
    }

    return list.get(0);
  }

  private DinaGroupDto convertFromRepresentation(final GroupRepresentation groupRep) {
    if (groupRep == null) {
      log.error("cannot convert null group");
      return null;
    }

    DinaGroupDto.DinaGroupDtoBuilder builder = DinaGroupDto.builder()
      .internalId(groupRep.getId())
      .name(groupRep.getName())
      .path(groupRep.getPath());

    final Map<String, List<String>> attributes = groupRep.getAttributes();

    if (attributes == null) {
      log.warn("group '{}' has no attribute map", groupRep.getName());
    } else {
      log.info("Getting labels for group '{}'", groupRep.getName());
      for (final String key : attributes.keySet()) {
        if (key.startsWith(LABEL_ATTR_KEY_PREFIX)) {
          final String languageCode = key.substring(LABEL_ATTR_KEY_PREFIX.length());
          final String label = getStringAttr(attributes, key);
          log.debug("Label for language {}: {}", languageCode, label);

          builder = builder.label(languageCode, label);
        }
      }
    }

    return builder.build();
  }

  @Override
  @Cacheable(cacheNames = GROUPS_CACHE_NAME)
  public List<DinaGroupDto> getGroups() {
    return getGroups(null, null);
  }

  @Override
  @Cacheable(cacheNames = GROUPS_CACHE_NAME)
  public List<DinaGroupDto> getGroups(final Integer firstResult, final Integer maxResults) {
    log.debug("getting raw group list from {} ({} max)", firstResult, maxResults);
    final List<GroupRepresentation> rawGroups = getGroupsResource().groups(null, firstResult, maxResults, false);

    log.debug("converting groups");
    final List<DinaGroupDto> cookedGroups = rawGroups
      .stream()
      .map(this::convertFromRepresentation)
      .collect(Collectors.toList());

    log.debug("done converting groups; returning");
    return cookedGroups;
  }

  @Override
  @Cacheable(cacheNames = GROUPS_CACHE_NAME)
  public DinaGroupDto getGroup(String id) {
    log.debug("getting group {}", id);

    final GroupResource groupRes = getGroupsResource().group(id);

    if (groupRes == null) {
      log.error("No group with id {}", id);
      return null;
    }

    final GroupRepresentation groupRep = groupRes.toRepresentation();

    if (groupRep == null) {
      log.error("Group with id {} has no representation", id);
      return null;
    }

    return convertFromRepresentation(groupRep);
  }

  /**
   * Get the group membership based on the groupName.
   * Only base groups are supported, no subgroups.
   * @param identifier the uuid or the name of the group
   * @return DinaGroupMembershipDto or null if not found
   */
  @Override
  public DinaGroupMembershipDto getGroupMembership(String identifier) {

    String groupName;
    // Check if it's a UUID first
    if (DinaUserService.UUID_REGEX.matcher(identifier).matches()) {
      GroupResource groupResFromUUID = getGroupsResource().group(identifier);
      groupName = groupResFromUUID.toRepresentation().getName();
    } else {
      groupName = identifier;
    }

    // We only handle base group so, we need to control the path
    String subgroupNameForRole =
      StringUtils.remove(groupName, "/") + "/" + DinaRole.SUPER_USER.getKeycloakRoleName();

    GroupRepresentation subgroupForRole;
    try {
      subgroupForRole = getRealmResource().getGroupByPath(subgroupNameForRole);
    } catch (NotFoundException nfEx) {
      log.error("No subgroupForRole with id path {}", subgroupNameForRole);
      return null;
    }

    GroupResource groupRes = getGroupsResource().group(subgroupForRole.getId());

    List<UserRepresentation> members = groupRes.members(0, MAX_GROUP_MEMBER_PAGE_SIZE);
    if (members.size() == MAX_GROUP_MEMBER_PAGE_SIZE) {
      log.error("Max page size number for group {}", subgroupNameForRole);
      return null;
    }

    List<DinaUserSummaryDto> managedBy = members.stream().map(
      ur ->
        DinaUserSummaryDto.builder()
          .username(ur.getUsername())
          .agentId(DinaUserService.getAgentId(ur.getAttributes()))
          .build()).toList();

    return DinaGroupMembershipDto.builder()
      .name(groupName)
      .managedBy(managedBy)
      .build();
  }

  /**
   * Create a new group within the realm.
   * @param groupDto
   * @return
   */
  @CacheEvict(cacheNames = GROUPS_CACHE_NAME, allEntries = true)
  public DinaGroupDto createGroup(DinaGroupDto groupDto) {

    if (!GROUP_NAME_REGEX.matcher(groupDto.getName()).matches()) {
      throw new IllegalArgumentException("Invalid name");
    }

    GroupRepresentation newGroup = new GroupRepresentation();
    newGroup.setName(groupDto.getName());

    // handle group labels
    for (var entry : groupDto.getLabels().entrySet()) {
      if (entry.getKey().length() == 2) {
        newGroup.singleAttribute(LABEL_ATTR_KEY_PREFIX + entry.getKey(), entry.getValue());
      }
    }

    try (Response response = getGroupsResource().add(newGroup)) {
      if (!isSuccessful(response)) {
        log.error("Failed to create group {}. Returned code {}", groupDto.getName(), response.getStatusInfo().getStatusCode());
        throw new IllegalStateException(response.getStatusInfo().getReasonPhrase());
      }
      String groupId = getCreatedId(response);
      newGroup.setId(groupId);
    }

    GroupResource grpResource = getGroupsResource().group(newGroup.getId());

    // create all the subgroups per role
    for (DinaRole dr : DinaRole.values()) {
      // Skip non group-based roles
      if (!NON_GROUP_BASED_ROLES.contains(dr)) {
        createDinaSubGroup(grpResource, dr);
      }
    }
    return convertFromRepresentation(grpResource.toRepresentation());
  }

  /**
   * Create a Keycloak subgroup based on DinaRole.
   *
   * @param groupResource
   * @param role
   */
  private void createDinaSubGroup(GroupResource groupResource, DinaRole role) {
    GroupRepresentation subGroup = new GroupRepresentation();
    subGroup.setName(role.getKeycloakRoleName());

    try (Response response = groupResource.subGroup(subGroup)) {
      String groupId = getCreatedId(response);
      subGroup.setId(groupId);
      log.debug("Created Subgroup : " + role.getKeycloakRoleName());
    }
  }

  private static boolean isSuccessful(Response response) {
    return response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL;
  }

}
