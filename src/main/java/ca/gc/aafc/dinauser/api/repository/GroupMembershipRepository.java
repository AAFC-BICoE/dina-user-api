package ca.gc.aafc.dinauser.api.repository;

import org.springframework.boot.info.BuildProperties;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.gc.aafc.dina.dto.JsonApiDto;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.repository.JsonApiModelAssistant;
import ca.gc.aafc.dina.security.TextHtmlSanitizer;
import ca.gc.aafc.dinauser.api.dto.DinaGroupMembershipDto;
import ca.gc.aafc.dinauser.api.service.DinaGroupService;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;

/**
 * Group membership is using the uuid or name of the group as key.
 */
@RestController
@RequestMapping(value = "${dina.apiPrefix:}", produces = JSON_API_VALUE)
public class GroupMembershipRepository {

  private static final String TYPE = DinaGroupMembershipDto.TYPENAME;

  private final DinaGroupService service;
  private final JsonApiModelAssistant<DinaGroupMembershipDto> jsonApiModelAssistant;

  public GroupMembershipRepository(DinaGroupService service,
                                   BuildProperties buildProperties) {
    this.service = service;
    this.jsonApiModelAssistant = new JsonApiModelAssistant<>(buildProperties.getVersion());
  }

  @GetMapping(TYPE + "/{idOrName}")
  public ResponseEntity<RepresentationModel<?>> onFindOne(@PathVariable String idOrName)
    throws ca.gc.aafc.dina.exception.ResourceNotFoundException {

    if (!TextHtmlSanitizer.isSafeText(idOrName)) {
      throw new IllegalArgumentException("unsafe value detected in attribute");
    }

    DinaGroupMembershipDto groupMembershipDto = service.getGroupMembership(idOrName);
    if (groupMembershipDto == null) {
      throw ResourceNotFoundException.create(TYPE, idOrName);
    }

    JsonApiDto.JsonApiDtoBuilder<DinaGroupMembershipDto> jsonApiDtoBuilder =
      JsonApiDto.<DinaGroupMembershipDto>builder().dto(groupMembershipDto);

    return ResponseEntity.ok(jsonApiModelAssistant.createJsonApiModelBuilder(jsonApiDtoBuilder.build()).build());
  }

}
