package ca.gc.aafc.dinauser.api.repository;

import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.service.DinaUserService;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.Collection;

@Repository
public class UserRepository extends ResourceRepositoryBase<DinaUserDto, String> {

  @Inject
  private DinaUserService userRepository;

  public UserRepository() {
    super(DinaUserDto.class);
  }

  @Override
  public ResourceList<DinaUserDto> findAll(QuerySpec querySpec) {
    return this.findAll(null, querySpec);
  }

  @Override
  public DinaUserDto findOne(String id, QuerySpec querySpec) {
    return userRepository.getUser(id);
  }

  @Override
  public ResourceList<DinaUserDto> findAll(
    Collection<String> ids, QuerySpec querySpec
  ) {
    return querySpec.apply(userRepository.getUsers());
  }

  @Override
  public <S extends DinaUserDto> S save(S resource) {
    userRepository.updateUser(resource);
    return resource;
  }

  @Override
  public <S extends DinaUserDto> S create(S resource) {
    userRepository.createUser(resource);
    return resource;
  }

  @Override
  public void delete(String id) {
    userRepository.deleteUser(id);
    super.delete(id);
  }
}
