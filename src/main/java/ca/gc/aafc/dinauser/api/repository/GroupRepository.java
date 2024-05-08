package ca.gc.aafc.dinauser.api.repository;

import ca.gc.aafc.dina.repository.meta.DinaMetaInfo;
import ca.gc.aafc.dina.security.TextHtmlSanitizer;
import io.crnk.core.repository.MetaRepository;
import io.crnk.core.resource.meta.MetaInformation;

import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Repository;

import ca.gc.aafc.dinauser.api.dto.DinaGroupDto;
import ca.gc.aafc.dinauser.api.security.GroupManagementAuthorizationService;
import ca.gc.aafc.dinauser.api.service.DinaGroupService;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

import java.util.Collection;

@Repository
public class GroupRepository extends ResourceRepositoryBase<DinaGroupDto, String> implements
    MetaRepository<DinaGroupDto> {

  private final DinaGroupService service;
  private final GroupManagementAuthorizationService authorizationService;
  private final BuildProperties buildProperties;
  
  public GroupRepository(DinaGroupService service,
                         GroupManagementAuthorizationService authorizationService,
                         BuildProperties buildProperties) {
    super(DinaGroupDto.class);
    this.service = service;
    this.authorizationService = authorizationService;
    this.buildProperties = buildProperties;
  }

  @Override
  public ResourceList<DinaGroupDto> findAll(QuerySpec querySpec) {
    return querySpec.apply(service.getGroups());
  }
  
  @Override
  public DinaGroupDto findOne(String id, QuerySpec querySpec) {
    if(!TextHtmlSanitizer.isSafeText(id)) {
      throw new IllegalArgumentException("unsafe value detected in attribute");
    }
    return service.getGroup(id);
  }

  @Override
  public <S extends DinaGroupDto> S create(S resource) {
    authorizationService.authorizeCreate(resource);
    return (S) service.createGroup(resource);
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
