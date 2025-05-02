package ca.gc.aafc.dinauser.api.service;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import ca.gc.aafc.dinauser.api.dto.DinaGroupDto;
import ca.gc.aafc.dinauser.api.dto.DinaGroupMembershipDto;

public interface DinaGroupService {

  List<DinaGroupDto> getGroups();

  List<DinaGroupDto> getGroups(Integer firstResult, Integer maxResults);

  List<DinaGroupDto> getGroups(Integer firstResult, Integer maxResults, Predicate<DinaGroupDto> predicate, Comparator<DinaGroupDto> comparator);

  DinaGroupDto getGroup(String id);

  DinaGroupDto createGroup(DinaGroupDto groupDto);

  DinaGroupMembershipDto getGroupMembership(String identifier);

}
