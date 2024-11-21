package ca.gc.aafc.dinauser.api.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import ca.gc.aafc.dinauser.api.dto.DinaUserDto;

public interface DinaUserService {

  String AGENT_ID_ATTR_KEY = "agentId";
  Pattern UUID_REGEX = Pattern.compile(
    "[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}");

  static String getAgentId(Map<String, List<String>> attributes) {
    if (attributes == null) {
      return null;
    }

    List<String> agentIds = attributes.get(AGENT_ID_ATTR_KEY);
    if (agentIds == null || agentIds.isEmpty()) {
      return null;
    }
    return agentIds.getFirst();
  }

  List<DinaUserDto> getAllUsers();

  List<DinaUserDto> getUsers(Integer firstResult, Integer maxResults);

  List<DinaUserDto> getUsers(Set<String> groups);

  DinaUserDto findOne(Object naturalId);

  DinaUserDto create(DinaUserDto entity);

  void deleteUser(String id);

  boolean exists(Object naturalId);

}
