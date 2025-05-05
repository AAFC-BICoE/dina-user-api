package ca.gc.aafc.dinauser.api.repository;

import org.apache.commons.collections.CollectionUtils;
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
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.filter.FilterComponent;
import ca.gc.aafc.dina.filter.QueryComponent;
import ca.gc.aafc.dina.filter.QueryStringParser;
import ca.gc.aafc.dina.filter.SimpleObjectFilterHandlerV2;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
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
import java.util.function.Predicate;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "${dina.apiPrefix:}", produces = JSON_API_VALUE)
public class GroupRepository {

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
    FilterComponent fc = queryComponents.getFilters();

    Predicate<DinaGroupDto> predicate = SimpleObjectFilterHandlerV2.createPredicate(fc);

    Comparator<DinaGroupDto> comparator = null;
    if (CollectionUtils.isNotEmpty(queryComponents.getSorts())) {
      for (String sort : queryComponents.getSorts()) {
        if (comparator == null) {
          comparator = SimpleObjectFilterHandlerV2.generateComparator(sort);
        } else {
          comparator =
            comparator.thenComparing(SimpleObjectFilterHandlerV2.generateComparator(sort));
        }
      }
    }

    int limit = DinaRepositoryV2.toSafePageLimit(queryComponents.getPageLimit());
    int offset = DinaRepositoryV2.toSafePageLimit(queryComponents.getPageOffset());

    List<DinaGroupDto> groups = service.getGroups(offset, limit, predicate, comparator);

    // transform in JsonApiDto for consistency
    List<JsonApiDto<DinaGroupDto>> jsonApiDtos = groups.stream()
      .map(dto -> JsonApiDto.<DinaGroupDto>builder().dto(dto).build())
      .toList();

    JsonApiModelBuilder builder = jsonApiModelAssistant.createJsonApiModelBuilder(
      new DinaRepositoryV2.PagedResource<>(offset,limit, groups.size(), jsonApiDtos)
    );

    return ResponseEntity.ok(builder.build());
  }

  @GetMapping(TYPE + "/{id}")
  public ResponseEntity<RepresentationModel<?>> onFindOne(@PathVariable UUID id)
      throws ResourceNotFoundException {

    DinaGroupDto group = service.getGroup(id.toString());
    if (group == null) {
      throw ResourceNotFoundException.create(GroupRepository.class.getSimpleName(), id);
    }

    JsonApiDto.JsonApiDtoBuilder<DinaGroupDto> jsonApiDtoBuilder = JsonApiDto.builder();
    jsonApiDtoBuilder.dto(group);

    return ResponseEntity.ok(jsonApiModelAssistant.createJsonApiModelBuilder(jsonApiDtoBuilder.build()).build());
  }

  @PostMapping(TYPE)
  public ResponseEntity<RepresentationModel<?>> onCreate(@RequestBody JsonApiDocument postedDocument) {

    if (postedDocument == null) {
      return ResponseEntity.badRequest().build();
    }
    DinaGroupDto dto = objMapper.convertValue(postedDocument.getAttributes(), DinaGroupDto.class);
    authorizationService.authorizeCreate(dto);

    DinaGroupDto createdGroup = service.createGroup(dto);

    if (createdGroup != null) {
      JsonApiDto.JsonApiDtoBuilder<DinaGroupDto> jsonApiDtoBuilder =
        JsonApiDto.<DinaGroupDto>builder().dto(createdGroup);

      JsonApiModelBuilder builder = jsonApiModelAssistant.createJsonApiModelBuilder(jsonApiDtoBuilder.build());
      builder.link(generateLinkToResource(createdGroup));

      RepresentationModel<?> model = builder.build();
      URI uri = model.getRequiredLink(IanaLinkRelations.SELF).toUri();

      return ResponseEntity.created(uri).body(model);
    }
    return ResponseEntity.internalServerError().build();
  }

}
