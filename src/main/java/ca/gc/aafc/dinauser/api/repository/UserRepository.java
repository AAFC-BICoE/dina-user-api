package ca.gc.aafc.dinauser.api.repository;

import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.security.DinaRole;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.service.DinaUserService;
import io.crnk.core.exception.MethodNotAllowedException;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.Collection;

@Repository
public class UserRepository extends ResourceRepositoryBase<DinaUserDto, String> {

  @Inject
  private DinaUserService service;
  private final DinaAuthenticatedUser authenticatedUser;

  public UserRepository(DinaAuthenticatedUser authenticatedUser) {
    super(DinaUserDto.class);
    this.authenticatedUser = authenticatedUser;
  }

  @Override
  public ResourceList<DinaUserDto> findAll(QuerySpec querySpec) {
    if (!isCollectionManager(authenticatedUser)) {
      querySpec.addFilter(
        PathSpec.of("agentId").filter(FilterOperator.EQ, authenticatedUser.getAgentIdentifer()));
    }
    return this.findAll(null, querySpec);
  }

  @Override
  public DinaUserDto findOne(String id, QuerySpec querySpec) {
    return service.getUser(id);
  }

  @Override
  public ResourceList<DinaUserDto> findAll(Collection<String> ids, QuerySpec querySpec) {
    return querySpec.apply(service.getUsers());
  }

  @Override
  public <S extends DinaUserDto> S save(S resource) {
    throw new MethodNotAllowedException("patch is currently not supported");
  }

  @Override
  public <S extends DinaUserDto> S create(S resource) {
    throw new MethodNotAllowedException("post is currently not supported");
  }

  @Override
  public void delete(String id) {
    service.deleteUser(id);
  }

  private static boolean isCollectionManager(DinaAuthenticatedUser authenticatedUser) {
    return authenticatedUser.getRolesPerGroup().values().stream()
      .anyMatch(dinaRoles -> dinaRoles.stream()
        .anyMatch(dinaRole -> dinaRole.equals(DinaRole.COLLECTION_MANAGER)));
  }
}
