package ca.gc.aafc.dinauser.api.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.security.DevUserConfig;
import ca.gc.aafc.dina.security.DinaRole;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;

@Service
@ConditionalOnProperty(value = "dev-user.enabled", havingValue = "true")
@Log4j2
public class DevUserService implements DinaUserService {

  private final DinaUserDto devUserDto;

  public DevUserService(DevUserConfig devUserConfig) {
    // map DineRole to its string value
    Map<String, Set<String>> rolesPerGroup = new HashMap<>();
    for (var entry : devUserConfig.getRolesPerGroup().entrySet()) {
      rolesPerGroup.put(entry.getKey(),
        entry.getValue().stream().map(DinaRole::getKeycloakRoleName).collect(
          Collectors.toSet()));
    }

    devUserDto = DinaUserDto.builder()
      .username(devUserConfig.getUsername())
      .internalId(devUserConfig.getInternalId())
      .rolesPerGroup(rolesPerGroup)
      .build();
  }

  @Override
  public List<DinaUserDto> getAllUsers() {
    return List.of(devUserDto);
  }

  @Override
  public List<DinaUserDto> getUsers(Integer firstResult, Integer maxResults) {
    return getAllUsers();
  }

  @Override
  public List<DinaUserDto> getUsers(Set<String> groups) {
    if (CollectionUtils.isProperSubCollection(groups, devUserDto.getRolesPerGroup().keySet())) {
      return List.of(devUserDto);
    }
    return List.of();
  }

  @Override
  public DinaUserDto findOne(Object naturalId) {
    if (devUserDto.getInternalId().equals(Objects.toString(naturalId))) {
      return devUserDto;
    }
    return null;
  }

  @Override
  public DinaUserDto create(DinaUserDto entity) {
    return null;
  }

  @Override
  public void deleteUser(String id) {
    // no-op
  }

  @Override
  public boolean exists(Object naturalId) {
    return false;
  }
}
