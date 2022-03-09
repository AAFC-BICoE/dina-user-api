package ca.gc.aafc.dinauser.api.repository;

import ca.gc.aafc.dina.repository.meta.DinaMetaInfo;
import io.crnk.core.repository.MetaRepository;
import io.crnk.core.resource.meta.MetaInformation;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Repository;

import ca.gc.aafc.dinauser.api.dto.DinaGroupDto;
import ca.gc.aafc.dinauser.api.service.DinaGroupService;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

import java.util.Collection;

@Repository
public class GroupRepository extends ResourceRepositoryBase<DinaGroupDto, String> implements
    MetaRepository<DinaGroupDto> {

  private final DinaGroupService service;
  private final BuildProperties buildProperties;
  
  public GroupRepository(DinaGroupService service, BuildProperties buildProperties) {
    super(DinaGroupDto.class);
    this.service = service;
    this.buildProperties = buildProperties;
  }

  @Override
  public ResourceList<DinaGroupDto> findAll(QuerySpec querySpec) {
    return querySpec.apply(service.getGroups());
  }
  
  @Override
  public DinaGroupDto findOne(String id, QuerySpec querySpec) {
    return service.getGroup(id);
  }

  @Override
  public MetaInformation getMetaInformation(Collection<DinaGroupDto> resources,
      QuerySpec querySpec, MetaInformation current) {
    DinaMetaInfo metaInfo = new DinaMetaInfo();
    metaInfo.setTotalResourceCount((long)resources.size());
    metaInfo.setModuleVersion(this.buildProperties.getVersion());
    return metaInfo;
  }
}
