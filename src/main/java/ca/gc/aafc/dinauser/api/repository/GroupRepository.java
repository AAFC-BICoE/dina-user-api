package ca.gc.aafc.dinauser.api.repository;

import javax.inject.Inject;

import org.springframework.stereotype.Repository;

import ca.gc.aafc.dinauser.api.dto.DinaGroupDto;
import ca.gc.aafc.dinauser.api.service.DinaGroupService;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

@Repository
public class GroupRepository extends ResourceRepositoryBase<DinaGroupDto, String> {
  
  @Inject
  private DinaGroupService service;
  
  public GroupRepository() {
    super(DinaGroupDto.class);
  }

  @Override
  public ResourceList<DinaGroupDto> findAll(QuerySpec querySpec) {
    return querySpec.apply(service.getGroups());
  }
  
  @Override
  public DinaGroupDto findOne(String id, QuerySpec querySpec) {
    return service.getGroup(id);
  }

}
