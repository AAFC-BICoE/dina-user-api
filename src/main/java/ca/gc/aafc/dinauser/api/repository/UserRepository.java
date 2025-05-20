package ca.gc.aafc.dinauser.api.repository;

import org.springframework.boot.info.BuildProperties;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;

import ca.gc.aafc.dina.dto.JsonApiDto;
import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.filter.FilterComponent;
import ca.gc.aafc.dina.filter.QueryComponent;
import ca.gc.aafc.dina.filter.QueryStringParser;
import ca.gc.aafc.dina.filter.SimpleObjectFilterHandlerV2;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.repository.DinaRepositoryLayer;
import ca.gc.aafc.dina.repository.DinaRepositoryV2;
import ca.gc.aafc.dina.repository.JsonApiModelAssistant;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.security.DinaRole;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.security.UserAuthorizationService;
import ca.gc.aafc.dinauser.api.service.DinaUserService;

import static ca.gc.aafc.dina.repository.DinaRepositoryV2.decodeQueryString;
import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import lombok.NonNull;

@RestController
@RequestMapping(value = "${dina.apiPrefix:}", produces = JSON_API_VALUE)
public class UserRepository implements DinaRepositoryLayer<UUID, DinaUserDto> {

  private static final String TYPE = DinaUserDto.TYPENAME;

  private final DinaUserService service;

  private final JsonApiModelAssistant<DinaUserDto> jsonApiModelAssistant;

  private final DinaAuthenticatedUser user;
  private final UserAuthorizationService authService;
  private final ObjectMapper objMapper;

  public UserRepository(
          @NonNull DinaUserService dinaService,
          @NonNull DinaAuthenticatedUser authenticatedUser,
          @NonNull UserAuthorizationService authService,
          @NonNull BuildProperties buildProperties,
          @NonNull ObjectMapper objectMapper
  ) {
    this.service = dinaService;
    this.user = authenticatedUser;
    this.authService = authService;
    this.objMapper = objectMapper;
    this.jsonApiModelAssistant = new JsonApiModelAssistant<>(buildProperties.getVersion());
  }


  protected Link generateLinkToResource(DinaUserDto dto) {
    try {
      return linkTo(methodOn(UserRepository.class).onFindOne(dto.getJsonApiId())).withSelfRel();
    } catch (ResourceNotFoundException | ResourceGoneException e) {
      throw new RuntimeException(e);
    }
  }

  @GetMapping(TYPE + "/{id}")
  public ResponseEntity<RepresentationModel<?>> onFindOne(@PathVariable UUID id)
      throws ResourceNotFoundException, ResourceGoneException {
    JsonApiDto<DinaUserDto> dto = getOne(id);
    return ResponseEntity.ok(jsonApiModelAssistant.createJsonApiModelBuilder(dto).build());
  }

  /**
   *
   * @param id
   * @param queryString will be ignored, not used
   * @return
   * @throws ResourceGoneException
   * @throws ResourceNotFoundException
   */
  @Override
  public JsonApiDto<DinaUserDto> getOne(UUID id, String queryString)
      throws ResourceGoneException, ResourceNotFoundException {
    return getOne(id);
  }

  public JsonApiDto<DinaUserDto> getOne(UUID id) throws ResourceNotFoundException, ResourceGoneException {
    DinaUserDto fetched = service.findOne(id);
    authService.authorizeFindOne(fetched);

    if (fetched == null) {
      throw ResourceNotFoundException.create(TYPE, id);
    }

    JsonApiDto.JsonApiDtoBuilder<DinaUserDto> jsonApiDtoBuilder = JsonApiDto.builder();
    jsonApiDtoBuilder.dto(fetched);
    return jsonApiDtoBuilder.build();
  }

  @GetMapping(TYPE)
  public ResponseEntity<RepresentationModel<?>> onFindAll(HttpServletRequest req) {

    String queryString = req != null ? decodeQueryString(req) : null;
    QueryComponent queryComponents = QueryStringParser.parse(queryString);

    DinaRepositoryV2.PagedResource<JsonApiDto<DinaUserDto>> dtos = getAll(queryComponents);

    return ResponseEntity.ok(jsonApiModelAssistant
      .createJsonApiModelBuilder(getAll(queryComponents)).build());
  }

  public DinaRepositoryV2.PagedResource<JsonApiDto<DinaUserDto>> getAll(QueryComponent queryComponents) {
    FilterComponent fc = queryComponents.getFilters();

    Predicate<DinaUserDto> predicate = SimpleObjectFilterHandlerV2.createPredicate(fc);
    Comparator<DinaUserDto> comparator = SimpleObjectFilterHandlerV2.generateComparator(queryComponents.getSorts());

    int limit = DinaRepositoryV2.toSafePageLimit(queryComponents.getPageLimit());
    int offset = DinaRepositoryV2.toSafePageOffset(queryComponents.getPageOffset());

    List<DinaUserDto> userDtos = new ArrayList<>();
    // 1. handle dina admin
    if (user.getAdminRoles().contains(DinaRole.DINA_ADMIN)) {
      userDtos = service.getAllUsers(predicate, comparator);
    } else if (!user.getGroupsForMinimumRole(DinaRole.SUPER_USER).isEmpty()) {
      // 2. get groups where the user is at least super user
      userDtos =
        service.getUsers(user.getGroupsForMinimumRole(DinaRole.SUPER_USER), predicate, comparator);
      // 3. otherwise only the authenticated user.
    } else {
      userDtos.add(service.findOne(user.getInternalIdentifier()));
    }

    List<JsonApiDto<DinaUserDto>> dtos = new ArrayList<>(userDtos.size());
    for (DinaUserDto dto : userDtos) {
      dtos.add(JsonApiDto.<DinaUserDto>builder().dto(dto).build());
    }

    return new DinaRepositoryV2.PagedResource<>(offset, limit, dtos.size(), dtos);
  }

  @PostMapping(TYPE)
  @Transactional
  public ResponseEntity<RepresentationModel<?>> onCreate(@RequestBody JsonApiDocument postedDocument) {
    if (postedDocument == null) {
      return ResponseEntity.badRequest().build();
    }

    JsonApiDto<DinaUserDto> jsonApiDto = create(postedDocument, null);
    JsonApiModelBuilder builder = jsonApiModelAssistant.createJsonApiModelBuilder(jsonApiDto);
    builder.link(generateLinkToResource(jsonApiDto.getDto()));

    RepresentationModel<?> model = builder.build();
    URI uri = model.getRequiredLink(IanaLinkRelations.SELF).toUri();

    return ResponseEntity.created(uri).body(model);
  }

  @Override
  public JsonApiDto<DinaUserDto> create(JsonApiDocument docToCreate,
                                        Consumer<DinaUserDto> dtoCustomizer) {
    DinaUserDto dto = objMapper.convertValue(docToCreate.getAttributes(), DinaUserDto.class);
    authService.authorizeUpdateOnResource(dto);

    DinaUserDto createdDto = service.create(dto);

    JsonApiDto<DinaUserDto> reloadedDto;
    try {
      reloadedDto = getOne(createdDto.getJsonApiId());
    } catch (ResourceNotFoundException | ResourceGoneException e) {
      throw new RuntimeException(e);
    }

    return reloadedDto;
  }

  @PatchMapping(TYPE + "/{id}")
  @Transactional
  public ResponseEntity<RepresentationModel<?>> onUpdate(@RequestBody JsonApiDocument partialPatchDto,
                                                         @PathVariable UUID id)
      throws ResourceNotFoundException, ResourceGoneException {
    // Sanity check
    if (!Objects.equals(id, partialPatchDto.getId())) {
      return ResponseEntity.badRequest().build();
    }

    JsonApiDto<DinaUserDto> jsonApiDto = update(partialPatchDto);

    JsonApiModelBuilder builder = jsonApiModelAssistant.createJsonApiModelBuilder(jsonApiDto);

    return ResponseEntity.ok().body(builder.build());
  }

  @Override
  public JsonApiDto<DinaUserDto> update(JsonApiDocument docToUpdate)
      throws ResourceNotFoundException, ResourceGoneException {
    DinaUserDto dto = objMapper.convertValue(docToUpdate.getAttributes(), DinaUserDto.class);
    dto.setInternalId(docToUpdate.getIdAsStr());
    authService.authorizeUpdate(dto);
    service.update(dto);

    // reload dto
    return getOne(docToUpdate.getId());
  }

  @Override
  public void delete(UUID id) throws ResourceNotFoundException, ResourceGoneException {
    DinaUserDto fetched = service.findOne(id);
    authService.authorizeDelete(fetched);
    service.deleteUser(id.toString());
  }

}

