package ca.gc.aafc.dinauser.api.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.jsonapi.JsonApiDocuments;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.dinauser.api.dto.NotificationDto;
import ca.gc.aafc.dinauser.api.entity.Notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import javax.validation.ValidationException;

public class NotificationRepositoryIT extends BaseRepositoryIT {

  private static final UUID TEST_USER_ID = UUID.fromString("1d472bf2-514c-40af-9a60-77d6510a39fb");

  @Inject
  private NotificationRepository repo;

  @WithMockKeycloakUser(internalIdentifier="1d472bf2-514c-40af-9a60-77d6510a39fb", groupRole = {"aafc:USER"})
  @Test
  @DisplayName("Should create a valid notification with all fields")
  void create_validResource_recordCreated() throws ResourceGoneException, ResourceNotFoundException {

    NotificationDto dto = createTestNotification("Welcome");
    dto.setMessage("Hi from ${app}, please visit ${help} for help.");
    dto.setMessageParams(Map.of("app",
      List.of(new Notification.MessageParam(Notification.MessageParamType.TEXT, "api")),
      "help",
      List.of(new Notification.MessageParam(Notification.MessageParamType.TEXT, "Help Page"),
        new Notification.MessageParam(Notification.MessageParamType.URL, "/help"))));

    UUID uuid = createWithRepository(dto, repo::onCreate);
    NotificationDto result = repo.getOne(uuid, "").getDto();
    assertNotNull(result.getUuid());
  }

  @WithMockKeycloakUser(internalIdentifier="1d472bf2-514c-40af-9a60-77d6510a39fb", groupRole = {"aafc:USER"})
  @Test
  @DisplayName("Should trigger exception on invalid params")
  void create_invalidParams_validationException() {
    NotificationDto dto = createTestNotification("Welcome");
    dto.setMessage("Hi from ${app}, please visit ${help} for help.");
    dto.setMessageParams(Map.of("app",
      List.of(new Notification.MessageParam(Notification.MessageParamType.TEXT, "api"))));
    assertThrows(ValidationException.class,  ()-> createWithRepository(dto, repo::onCreate));
  }
  
  @WithMockKeycloakUser(internalIdentifier="1d472bf2-514c-40af-9a60-77d6510a39fb", groupRole = {"aafc:USER"})
  @Test
  @DisplayName("Should create for another user be denied")
  void create_onAnotherUserIdentifier_denied() {
    NotificationDto dto = createTestNotification("Welcome");
    dto.setUserIdentifier(UUID.randomUUID());
    assertThrows(AccessDeniedException.class, () -> createWithRepository(dto, repo::onCreate));
  }

  @WithMockKeycloakUser(internalIdentifier="1d472bf2-514c-40af-9a60-77d6510a39fb", groupRole = {"aafc:USER"})
  @Test
  @DisplayName("Should update notification title, message, status")
  void update_changeContent_contentUpdated() throws ResourceGoneException, ResourceNotFoundException {
    NotificationDto dto = createTestNotification("Original Title");
    UUID uuid = createWithRepository(dto, repo::onCreate);

    NotificationDto toUpdate = repo.getOne(uuid, "").getDto();
    toUpdate.setTitle("Updated Title");
    toUpdate.setMessage("Updated Message");
    toUpdate.setStatus(Notification.Status.READ);

    JsonApiDocument docToUpdate = JsonApiDocuments.createJsonApiDocument(
      uuid, dto.getJsonApiType(),
      JsonAPITestHelper.toAttributeMap(toUpdate)
    );
    repo.onUpdate(docToUpdate, uuid);

    NotificationDto result = repo.getOne(uuid, "").getDto();
    assertEquals("Updated Title", result.getTitle());
    assertEquals("Updated Message", result.getMessage());
    assertEquals(Notification.Status.READ, result.getStatus());
  }

  private NotificationDto createTestNotification(String title) {
    return NotificationDto.builder()
      .userIdentifier(TEST_USER_ID)
      .status(Notification.Status.NEW)
      .type("info")
      .title(title)
      .message("Test message")
      .build();
  }
}
