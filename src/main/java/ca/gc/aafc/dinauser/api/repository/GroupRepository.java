package ca.gc.aafc.dinauser.api.repository;

import org.springframework.boot.info.BuildProperties;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
import ca.gc.aafc.dinauser.api.dto.DinaGroupDto;
import ca.gc.aafc.dinauser.api.security.GroupManagementAuthorizationService;
import ca.gc.aafc.dinauser.api.service.DinaGroupService;

import static ca.gc.aafc.dina.repository.DinaRepositoryV2.decodeQueryString;
import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "${dina.apiPrefix:}", produces = JSON_API_VALUE)
public class GroupRepository implements DinaRepositoryLayer<UUID, DinaGroupDto> {

  private static final String TYPE = DinaGroupDto.TYPENAME;

  private final DinaGroupService service;
  private final GroupManagementAuthorizationService authorizationService;
  private final ObjectMapper objMapper;

  private final JsonApiModelAssistant<DinaGroupDto> jsonApiModelAssistant;
  
  public GroupRepository(DinaGroupService service,
                         GroupManagementAuthorizationService authorizationService,
                         ObjectMapper objMapper,
                         BuildProperties buildProperties) {
    this.service = service;
    this.authorizationService = authorizationService;
    this.objMapper = objMapper;

    this.jsonApiModelAssistant = new JsonApiModelAssistant<>(buildProperties.getVersion());
  }

  protected Link generateLinkToResource(DinaGroupDto dto) {
    try {
      return linkTo(
        methodOn(GroupRepository.class).onFindOne(UUID.fromString(dto.getInternalId()))).withSelfRel();
    } catch (ResourceNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @GetMapping(TYPE)
  public ResponseEntity<RepresentationModel<?>> onFindAll(HttpServletRequest req) {
    String queryString = req != null ? decodeQueryString(req) : null;
    QueryComponent queryComponents = QueryStringParser.parse(queryString);

    DinaRepositoryV2.PagedResource<JsonApiDto<DinaGroupDto>> pagedResource = getAll(queryComponents);

    JsonApiModelBuilder builder = jsonApiModelAssistant
      .createJsonApiModelBuilder(pagedResource);

    return ResponseEntity.ok(builder.build());
  }

  @GetMapping(TYPE + "/{id}")
  public ResponseEntity<RepresentationModel<?>> onFindOne(@PathVariable UUID id)
      throws ResourceNotFoundException {
    
    JsonApiDto<DinaGroupDto> groupDto = getOne(id, null);
    return ResponseEntity.ok(jsonApiModelAssistant.createJsonApiModelBuilder(groupDto).build());
  }

  @PostMapping(TYPE)
  public ResponseEntity<RepresentationModel<?>> onCreate(@RequestBody JsonApiDocument postedDocument) {

    if (postedDocument == null) {
      return ResponseEntity.badRequest().build();
    }

    JsonApiDto<DinaGroupDto> createdGroup = create(postedDocument, null);
    if (createdGroup != null) {
      JsonApiModelBuilder builder = jsonApiModelAssistant.createJsonApiModelBuilder(createdGroup);
      builder.link(generateLinkToResource(createdGroup.getDto()));

      RepresentationModel<?> model = builder.build();
      URI uri = model.getRequiredLink(IanaLinkRelations.SELF).toUri();

      return ResponseEntity.created(uri).body(model);
    }
    return ResponseEntity.internalServerError().build();
  }

  @Override
  public JsonApiDto<DinaGroupDto> getOne(UUID identifier, String queryString)
    throws ResourceNotFoundException {
    DinaGroupDto group = service.getGroup(identifier.toString());
    if (group == null) {
      throw ResourceNotFoundException.create(GroupRepository.class.getSimpleName(), identifier);
    }

    JsonApiDto.JsonApiDtoBuilder<DinaGroupDto> jsonApiDtoBuilder = JsonApiDto.builder();
    jsonApiDtoBuilder.dto(group);

    return jsonApiDtoBuilder.build();
  }

  @Override
  public DinaRepositoryV2.PagedResource<JsonApiDto<DinaGroupDto>> getAll(QueryComponent queryComponents) {
    FilterComponent fc = queryComponents.getFilters();

    Predicate<DinaGroupDto> predicate = SimpleObjectFilterHandlerV2.createPredicate(fc);
    Comparator<DinaGroupDto> comparator = SimpleObjectFilterHandlerV2.generateComparator(queryComponents.getSorts());

    int limit = DinaRepositoryV2.toSafePageLimit(queryComponents.getPageLimit());
    int offset = DinaRepositoryV2.toSafePageOffset(queryComponents.getPageOffset());

    List<DinaGroupDto> groups = service.getGroups(offset, limit, predicate, comparator);

    // transform in JsonApiDto for consistency
    List<JsonApiDto<DinaGroupDto>> jsonApiDtos = groups.stream()
      .map(dto -> JsonApiDto.<DinaGroupDto>builder().dto(dto).build())
      .toList();

    return new DinaRepositoryV2.PagedResource<>(offset,limit, groups.size(), jsonApiDtos);
  }

  @Override
  public JsonApiDto<DinaGroupDto> create(JsonApiDocument docToCreate,
                                         Consumer<DinaGroupDto> dtoCustomizer) {
    DinaGroupDto dto = objMapper.convertValue(docToCreate.getAttributes(), DinaGroupDto.class);
    authorizationService.authorizeCreate(dto);

    DinaGroupDto createdGroup = service.createGroup(dto);

    if (createdGroup != null) {
      JsonApiDto.JsonApiDtoBuilder<DinaGroupDto> jsonApiDtoBuilder =
        JsonApiDto.<DinaGroupDto>builder().dto(createdGroup);
      return jsonApiDtoBuilder.build();
    }
    return null;
  }

  @Override
  public JsonApiDto<DinaGroupDto> update(JsonApiDocument patchDto)
    throws ResourceNotFoundException, ResourceGoneException {
    // not implemented
    return null;
  }

  @Override
  public void delete(UUID identifier) throws ResourceNotFoundException, ResourceGoneException {
    // not implemented
  }
}
