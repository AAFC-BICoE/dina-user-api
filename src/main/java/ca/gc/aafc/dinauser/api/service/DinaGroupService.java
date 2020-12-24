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
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class DinaGroupService {
  
  private static final String LABEL_EN_ATTR_KEY = "groupLabelEn";
  private static final String LABEL_FR_ATTR_KEY = "groupLabelFr";
  
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
    
    final DinaGroupDto group = new DinaGroupDto();
    
    group.setInternalId(groupRep.getId());
    group.setName(groupRep.getName());
    group.setPath(groupRep.getPath());
    
    final Map<String, List<String>> attributes = groupRep.getAttributes();
    if (attributes == null) {
      log.warn("group '{}' has no attribute map", groupRep.getName());
    } else {
      log.info("Getting English and French labels for group '{}'", groupRep.getName());
      group.setLabelEn(getStringAttr(attributes, LABEL_EN_ATTR_KEY));
      group.setLabelFr(getStringAttr(attributes, LABEL_FR_ATTR_KEY));
    }
    
    return group;
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
