package ca.gc.aafc.dinauser.api.repository;

import ca.gc.aafc.dina.filter.DinaFilterResolver;
import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.service.DinaUserService;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import lombok.NonNull;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

@Repository
public class UserRepository extends DinaRepository<DinaUserDto, DinaUserDto> {

  @Inject
  private DinaUserService service;

  public UserRepository(
    @NonNull DinaService<DinaUserDto> dinaService,
    @NonNull DinaFilterResolver filterResolver
  ) {
    super(
      dinaService,
      Optional.empty(),
      Optional.empty(),
      new DinaMapper<>(DinaUserDto.class),
      DinaUserDto.class,
      DinaUserDto.class,
      filterResolver,
      null);
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
  public ResourceList<DinaUserDto> findAll(Collection<Serializable> ids, QuerySpec querySpec) {
    return querySpec.apply(service.getUsers());
  }
}
