package ca.gc.aafc.dinauser.api.repository;

import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.repository.DinaRepositoryV2;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dinauser.api.dto.UserPreferenceDto;
import ca.gc.aafc.dinauser.api.entity.UserPreference;
import ca.gc.aafc.dinauser.api.security.UserPreferenceAuthorizationService;
import ca.gc.aafc.dinauser.api.service.UserPreferenceService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import lombok.NonNull;
import org.springframework.boot.info.BuildProperties;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.gc.aafc.dinauser.api.mapper.UserPreferenceMapper;

import java.util.Optional;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "${dina.apiPrefix:}", produces = JSON_API_VALUE)
public class UserPreferenceRepository extends DinaRepositoryV2<UserPreferenceDto, UserPreference> {

  private static final String TYPE = UserPreferenceDto.TYPENAME;

  // Bean does not exist with keycloak disabled.
  private final DinaAuthenticatedUser authenticatedUser;

  public UserPreferenceRepository(
    @NonNull UserPreferenceService userPreferenceService,
    UserPreferenceAuthorizationService authService,
    Optional<DinaAuthenticatedUser> authenticatedUser,
    @NonNull BuildProperties buildProperties,
    @NonNull ObjectMapper objectMapper
  ) {
    super(
      userPreferenceService,
      authService,
      Optional.empty(),
      UserPreferenceMapper.INSTANCE,
      UserPreferenceDto.class,
      UserPreference.class,
      buildProperties, objectMapper);
    this.authenticatedUser = authenticatedUser.orElse(null);
  }

  @Override
  protected Link generateLinkToResource(UserPreferenceDto dto) {
    try {
      return linkTo(methodOn(UserPreferenceRepository.class).onFindOne(dto.getUuid(), null)).withSelfRel();
    } catch (ResourceNotFoundException | ResourceGoneException e) {
      throw new RuntimeException(e);
    }
  }

  @GetMapping(TYPE + "/{id}")
  public ResponseEntity<RepresentationModel<?>> onFindOne(@PathVariable UUID id, HttpServletRequest req)
    throws ResourceNotFoundException, ResourceGoneException {
    return handleFindOne(id, req);
  }

  @GetMapping(TYPE)
  public ResponseEntity<RepresentationModel<?>> onFindAll(HttpServletRequest req) {
    return handleFindAll(req);
  }

  @PostMapping(TYPE)
  @Transactional
  public ResponseEntity<RepresentationModel<?>> onCreate(@RequestBody JsonApiDocument postedDocument) {
    return handleCreate(postedDocument, null);
  }

  @PatchMapping(TYPE + "/{id}")
  @Transactional
  public ResponseEntity<RepresentationModel<?>> onUpdate(@RequestBody JsonApiDocument partialPatchDto,
                                                         @PathVariable UUID id)
      throws ResourceNotFoundException, ResourceGoneException {
    return handleUpdate(partialPatchDto, id);
  }

  @DeleteMapping(TYPE + "/{id}")
  @Transactional
  public ResponseEntity<RepresentationModel<?>> onDelete(@PathVariable UUID id)
      throws ResourceNotFoundException, ResourceGoneException {
    return handleDelete(id);
  }
}
