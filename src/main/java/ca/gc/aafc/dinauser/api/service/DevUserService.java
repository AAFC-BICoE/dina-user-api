package ca.gc.aafc.dinauser.api.service;

import java.util.List;
import java.util.Set;
import lombok.extern.log4j.Log4j2;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.config.DevSettings;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;

@Service
@ConditionalOnProperty(value = "dev-user.enabled", havingValue = "true")
@Log4j2
public class DevUserService implements DinaUserService {

  private final DevSettings devSettings;

  public DevUserService(DevSettings devSettings) {
    this.devSettings = devSettings;
  }

  @Override
  public List<DinaUserDto> getAllUsers() {
    return List.of();
  }

  @Override
  public List<DinaUserDto> getUsers(Integer firstResult, Integer maxResults) {
    return List.of();
  }

  @Override
  public List<DinaUserDto> getUsers(Set<String> groups) {
    return List.of();
  }

  @Override
  public DinaUserDto findOne(Object naturalId) {
    return null;
  }

  @Override
  public DinaUserDto create(DinaUserDto entity) {
    return null;
  }

  @Override
  public void deleteUser(String id) {

  }

  @Override
  public boolean exists(Object naturalId) {
    return false;
  }
}
