package ca.gc.aafc.dinauser.api.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.security.DevUserConfig;
import ca.gc.aafc.dinauser.api.dto.DinaGroupDto;
import ca.gc.aafc.dinauser.api.dto.DinaGroupMembershipDto;

@Service
@ConditionalOnProperty(value = "dev-user.enabled", havingValue = "true")
@Log4j2
public class DevGroupService implements DinaGroupService {

  private final Map<String, DinaGroupDto> uuidToGroup;

  public DevGroupService(DevUserConfig devUserConfig) {
    
    // generate UUIDs for all groups
    Map<String, DinaGroupDto> uuidGroupBuilder = new HashMap<>();
    for (String group : devUserConfig.getGroups()) {
      String uuid = UUID.randomUUID().toString();
      uuidGroupBuilder.put(uuid, DinaGroupDto.builder().internalId(uuid).name(group).build());
    }
    uuidToGroup = Collections.unmodifiableMap(uuidGroupBuilder);
  }

  @Override
  public List<DinaGroupDto> getGroups() {
    return List.copyOf(uuidToGroup.values());
  }

  @Override
  public List<DinaGroupDto> getGroups(Integer firstResult, Integer maxResults) {
    // not enough groups in dev to support that
    return getGroups();
  }

  @Override
  public DinaGroupDto getGroup(String id) {
    return uuidToGroup.get(id);
  }

  @Override
  public DinaGroupDto createGroup(DinaGroupDto groupDto) {
    return null;
  }

  @Override
  public DinaGroupMembershipDto getGroupMembership(String identifier) {
    if (uuidToGroup.containsKey(identifier)) {
      return DinaGroupMembershipDto.builder().name(identifier).build();
    }
    return null;
  }
}
