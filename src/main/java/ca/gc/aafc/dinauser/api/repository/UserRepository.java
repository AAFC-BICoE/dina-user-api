package ca.gc.aafc.dinauser.api.repository;

import ca.gc.aafc.dina.filter.DinaFilterResolver;
import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.security.DinaRole;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.service.DinaUserService;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import lombok.NonNull;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public class UserRepository extends DinaRepository<DinaUserDto, DinaUserDto> {

  @Inject
  private DinaUserService service;
  private final DinaAuthenticatedUser user;

  public UserRepository(
    @NonNull DinaService<DinaUserDto> dinaService,
    @NonNull DinaAuthenticatedUser authenticatedUser,
    @NonNull DinaFilterResolver filterResolver,
    @NonNull BuildProperties props
  ) {
    super(
      dinaService,
      Optional.empty(),
      Optional.empty(),
      new DinaMapper<>(DinaUserDto.class),
      DinaUserDto.class,
      DinaUserDto.class,
      filterResolver,
      null,
      props);
    this.user = authenticatedUser;
  }

  @Override
  public ResourceList<DinaUserDto> findAll(QuerySpec querySpec) {
    return this.findAll(null, querySpec);
  }

  @Override
  public DinaUserDto findOne(Serializable id, QuerySpec querySpec) {
    return service.findOne(id, DinaUserDto.class);
  }

  @Override
  public ResourceList<DinaUserDto> findAll(Collection<Serializable> ids, QuerySpec qs) {
    if (getUserRoles().noneMatch(dinaRole -> DinaRole.COLLECTION_MANAGER.equals(dinaRole) ||
                                             DinaRole.DINA_ADMIN.equals(dinaRole))) {
      qs.getFilters().removeIf(f -> f.getAttributePath().get(0).equals("agentId"));
      qs.addFilter(PathSpec.of("agentId").filter(FilterOperator.EQ, user.getAgentIdentifer()));
    }
    return qs.apply(service.getUsers());
  }

  private Stream<DinaRole> getUserRoles() {
    return user.getRolesPerGroup()
      .values()
      .stream()
      .flatMap(Collection::stream);
  }
}
