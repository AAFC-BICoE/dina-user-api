package ca.gc.aafc.dinauser.api.service;

import java.util.List;

import ca.gc.aafc.dinauser.api.dto.DinaGroupDto;
import ca.gc.aafc.dinauser.api.dto.DinaGroupMembershipDto;

public interface DinaGroupService {

  List<DinaGroupDto> getGroups();

  List<DinaGroupDto> getGroups(Integer firstResult, Integer maxResults);

  DinaGroupDto getGroup(String id);

  DinaGroupDto createGroup(DinaGroupDto groupDto);

  DinaGroupMembershipDto getGroupMembership(String identifier);

}
