package ca.gc.aafc.dinauser.api.repository;

import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dinauser.api.dto.UserPreferenceDto;
import ca.gc.aafc.dinauser.api.entity.UserPreference;
import ca.gc.aafc.dinauser.api.security.UserPreferenceAuthorizationService;
import ca.gc.aafc.dinauser.api.service.UserPreferenceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserPreferenceRepository extends DinaRepository<UserPreferenceDto, UserPreference> {

  public UserPreferenceRepository(
    @NonNull UserPreferenceService userPreferenceService,
    UserPreferenceAuthorizationService authService,
    @NonNull BuildProperties buildProperties,
    @NonNull ObjectMapper objectMapper
  ) {
    super(
      userPreferenceService,
            authService,
      Optional.empty(),
      new DinaMapper<>(UserPreferenceDto.class),
      UserPreferenceDto.class,
      UserPreference.class,
      null,
      null,
      buildProperties, objectMapper);
  }
}
