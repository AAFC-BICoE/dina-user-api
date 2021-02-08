package ca.gc.aafc.dinauser.api.repository;

import ca.gc.aafc.dina.filter.DinaFilterResolver;
import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.dinauser.api.crudit.UserPreference;
import ca.gc.aafc.dinauser.api.dto.UserPreferenceDto;
import lombok.NonNull;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserPreferenceRepository extends DinaRepository<UserPreferenceDto, UserPreference> {

  public UserPreferenceRepository(
    @NonNull BaseDAO baseDAO,
    @NonNull DinaFilterResolver filterResolver,
    @NonNull BuildProperties buildProperties
  ) {
    super(
      new DefaultDinaService<>(baseDAO),
      Optional.empty(),
      Optional.empty(),
      new DinaMapper<>(UserPreferenceDto.class),
      UserPreferenceDto.class,
      UserPreference.class,
      filterResolver,
      null,
      buildProperties);
  }
}
