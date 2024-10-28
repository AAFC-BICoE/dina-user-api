package ca.gc.aafc.dinauser.api.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.config.DevSettings;
import ca.gc.aafc.dinauser.api.dto.DinaGroupDto;
import ca.gc.aafc.dinauser.api.dto.DinaGroupMembershipDto;

@Service
@ConditionalOnProperty(value = "dev-user.enabled", havingValue = "true")
@Log4j2
public class DevGroupService implements DinaGroupService {

  private final DevSettings devSettings;

  public DevGroupService(DevSettings devSettings) {
    this.devSettings = devSettings;
  }

  @Override
  public List<DinaGroupDto> getGroups() {
    return devSettings.getRolesPerGroup().keySet()
      .stream()
      .map(k -> DinaGroupDto.builder().name(k).build())
      .collect(Collectors.toList());
  }

  @Override
  public List<DinaGroupDto> getGroups(Integer firstResult, Integer maxResults) {
    return List.of();
  }

  @Override
  public DinaGroupDto getGroup(String id) {
    return null;
  }

  @Override
  public DinaGroupDto createGroup(DinaGroupDto groupDto) {
    return null;
  }

  @Override
  public DinaGroupMembershipDto getGroupMembership(String identifier) {
    return null;
  }
}
