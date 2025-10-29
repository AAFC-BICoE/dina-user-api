package ca.gc.aafc.dinauser.api.repository;

import org.junit.jupiter.api.Test;

import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.dinauser.api.dto.NotificationDto;
import ca.gc.aafc.dinauser.api.entity.Notification;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;

public class NotificationRepositoryIT extends BaseRepositoryIT {

  @Inject
  private NotificationRepository repo;

  @WithMockKeycloakUser(internalIdentifier="1d472bf2-514c-40af-9a60-77d6510a39fb", groupRole = {"aafc:USER"})
  @Test
  void create_validResource_recordCreated() throws ResourceGoneException, ResourceNotFoundException {

    NotificationDto dto = NotificationDto.builder()
      .userIdentifier(UUID.fromString("1d472bf2-514c-40af-9a60-77d6510a39fb"))
      .status(Notification.Status.NEW)
      .message("Hi from ${app}")
      .messageParams(Map.of("app", new Notification.MessageParam(Notification.MessageParamType.TEXT, "api")))
      .build();

    UUID uuid = createWithRepository(dto, repo::onCreate);
    NotificationDto result = repo.getOne(uuid, "").getDto();
    assertNotNull(result.getUuid());
  }
}
