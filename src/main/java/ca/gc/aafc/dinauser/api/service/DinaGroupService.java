package ca.gc.aafc.dinauser.api.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dinauser.api.dto.DinaGroupDto;
import ca.gc.aafc.dinauser.api.dto.DinaGroupDto.DinaGroupDtoBuilder;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class DinaGroupService {

  private static final String LABEL_ATTR_KEY_PREFIX = "groupLabel-";

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

    DinaGroupDtoBuilder builder = DinaGroupDto.builder()
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

  public List<DinaGroupDto> getGroups() {
    return getGroups(null, null);
  }

  public List<DinaGroupDto> getGroups(final Integer firstResult, final Integer maxResults) {
    log.debug("getting raw group list from {} ({} max)", firstResult, maxResults);
    final List<GroupRepresentation> rawGroups = getGroupsResource().groups(firstResult, maxResults);

    log.debug("converting groups");
    final List<DinaGroupDto> cookedGroups = rawGroups
        .stream()
        .map(g -> convertFromRepresentation(g))
        .collect(Collectors.toList());

    log.debug("done converting groups; returning");
    return cookedGroups;
  }

  public DinaGroupDto getGroup(final String id) {
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

}
