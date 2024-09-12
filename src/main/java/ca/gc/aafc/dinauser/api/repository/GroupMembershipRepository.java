package ca.gc.aafc.dinauser.api.repository;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.MetaRepository;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.MetaInformation;
import java.util.Collection;

import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Repository;

import ca.gc.aafc.dina.repository.meta.DinaMetaInfo;
import ca.gc.aafc.dina.security.TextHtmlSanitizer;
import ca.gc.aafc.dinauser.api.dto.DinaGroupMembershipDto;
import ca.gc.aafc.dinauser.api.service.DinaGroupService;

/**
 * Group membership is using the uuid or name of the group as key.
 */
@Repository
public class GroupMembershipRepository extends ResourceRepositoryBase<DinaGroupMembershipDto, String>
  implements MetaRepository<DinaGroupMembershipDto> {

  private final DinaGroupService service;
  private final BuildProperties buildProperties;

  public GroupMembershipRepository(DinaGroupService service,
                                   BuildProperties buildProperties) {
    super(DinaGroupMembershipDto.class);
    this.service = service;
    this.buildProperties = buildProperties;
  }

  @Override
  public DinaGroupMembershipDto findOne(String id, QuerySpec querySpec) {
    if (!TextHtmlSanitizer.isSafeText(id)) {
      throw new IllegalArgumentException("unsafe value detected in attribute");
    }

    DinaGroupMembershipDto groupMembershipDto = service.getGroupMembership(id);
    if (groupMembershipDto == null) {
      throw new ResourceNotFoundException("Group with name " + id + " Not Found.");
    }
    return groupMembershipDto;
  }

  @Override
  public ResourceList<DinaGroupMembershipDto> findAll(QuerySpec querySpec) {
    return null;
  }

  @Override
  public MetaInformation getMetaInformation(Collection<DinaGroupMembershipDto> resources,
                                            QuerySpec querySpec, MetaInformation metaInformation) {
    DinaMetaInfo metaInfo = new DinaMetaInfo();
    metaInfo.setTotalResourceCount((long)resources.size());
    metaInfo.setModuleVersion(this.buildProperties.getVersion());
    return metaInfo;
  }

}
