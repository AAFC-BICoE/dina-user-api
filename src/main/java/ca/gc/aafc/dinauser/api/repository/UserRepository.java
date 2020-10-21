package ca.gc.aafc.dinauser.api.repository;

import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository extends ResourceRepositoryBase<DinaUserDto, String> {

  public UserRepository() {
    super(DinaUserDto.class);
  }

  @Override
  public ResourceList<DinaUserDto> findAll(QuerySpec querySpec) {
    return null;
  }
}
