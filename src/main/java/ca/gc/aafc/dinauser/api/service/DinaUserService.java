package ca.gc.aafc.dinauser.api.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
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

  List<DinaUserDto> getAllUsers(Predicate<DinaUserDto> predicate, Comparator<DinaUserDto> sortComparator);

  List<DinaUserDto> getUsers(Integer firstResult, Integer maxResults,
                             Predicate<DinaUserDto> predicate, Comparator<DinaUserDto> sortComparator);

  List<DinaUserDto> getUsers(Set<String> groups, java.util.function.Predicate<DinaUserDto> predicate, Comparator<DinaUserDto> sortComparator);

  DinaUserDto findOne(Object naturalId);

  DinaUserDto create(DinaUserDto entity);

  DinaUserDto update(DinaUserDto entity);

  void deleteUser(String id);

  boolean exists(Object naturalId);

}
