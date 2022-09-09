package ca.gc.aafc.dinauser.api.repository;

import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.service.DinaUserService;
import ca.gc.aafc.dinauser.api.security.UserAuthorizationService;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import lombok.NonNull;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository extends DinaRepository<DinaUserDto, DinaUserDto> {

  @Inject
  private DinaUserService service;
  private final DinaAuthenticatedUser user;
  private final UserAuthorizationService authService;

  public UserRepository(
    @NonNull DinaService<DinaUserDto> dinaService,
    @NonNull DinaAuthenticatedUser authenticatedUser,
    @NonNull UserAuthorizationService authService,
    @NonNull BuildProperties props
  ) {
    super(
      dinaService,
      authService,
      Optional.empty(),
      new DinaMapper<>(DinaUserDto.class),
      DinaUserDto.class,
      DinaUserDto.class,
      null,
      null,
      props);
    this.user = authenticatedUser;
    this.authService = authService;
  }

  @Override
  public ResourceList<DinaUserDto> findAll(QuerySpec querySpec) {
    return this.findAll(null, querySpec);
  }

  @Override
  public DinaUserDto findOne(Serializable id, QuerySpec querySpec) {
    DinaUserDto fetched = service.findOne(id, DinaUserDto.class);
    authService.authorizeFindOne(fetched);
    return fetched;
  }

  @Override
  public ResourceList<DinaUserDto> findAll(Collection<Serializable> ids, QuerySpec qs) {
    QuerySpec filteredQuery = qs.clone();
    // if super-user of higher return all users
    if (UserAuthorizationService.isSuperUserOrHigher(user)) {
      return filteredQuery.apply(service.getUsers());
    }
    // otherwise only the authenticated user.
    return filteredQuery.apply(
            List.of(service.findOne(user.getInternalIdentifier(), DinaUserDto.class)));
  }

  @Override
  public <S extends DinaUserDto> S save(S resource) {
    authService.authorizeUpdateOnResource(resource);
    return super.save(resource);
  }

}

