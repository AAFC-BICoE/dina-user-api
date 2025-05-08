package ca.gc.aafc.dinauser.api.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.querydsl.core.types.Ops;

import ca.gc.aafc.dina.dto.JsonApiDto;
import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.filter.FilterExpression;
import ca.gc.aafc.dina.filter.QueryComponent;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.jsonapi.JsonApiDocuments;
import ca.gc.aafc.dina.repository.DinaRepositoryV2;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.dinauser.api.DinaUserModuleApiLauncher;
import ca.gc.aafc.dinauser.api.TestResourceHelper;
import ca.gc.aafc.dinauser.api.config.UserModuleTestConfiguration;
import ca.gc.aafc.dinauser.api.dto.DinaGroupDto;
import ca.gc.aafc.dinauser.api.dto.UserPreferenceDto;
import ca.gc.aafc.dinauser.api.service.DinaUserService;
import ca.gc.aafc.dinauser.api.testsupport.fixtures.UserPreferenceFixture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import javax.inject.Inject;
import javax.transaction.Transactional;

@SpringBootTest(classes = {UserModuleTestConfiguration.class, DinaUserModuleApiLauncher.class})
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@ContextConfiguration(initializers = PostgresTestContainerInitializer.class)
public class UserPreferenceRepositoryIT {

  public static final UUID TEST_USER_ID = UUID.fromString("1d472bf2-514c-40af-9a60-77d6510a39fb");

  public static final String TEST_RESOURCE_PATH = "src/test/resources/test-documents/";
  public static final String SAVED_SEARCH_RESOURCE = "user-preference-saved-search-data.json";
  public static final String UPDATED_SAVED_SEARCH_RESOURCE = "user-preference-saved-search-data-updated.json";

  @Inject
  private UserPreferenceRepository repo;

  @MockBean
  private DinaUserService userService;

  private UUID mockReferentialIntegrity(UUID uuid, boolean returnValue) {
    Mockito.when(userService.exists(uuid.toString())).thenReturn(returnValue);
    return uuid;
  }

  private UUID persistUserPreferenceDto(UUID expectedUserId) {

    JsonApiDocument docToCreate = JsonApiDocuments.createJsonApiDocument(
      UUID.randomUUID(), DinaGroupDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(UserPreferenceFixture.newUserPreferenceDto(expectedUserId)
        .savedSearches(
          TestResourceHelper.readContentAsJsonMap(TEST_RESOURCE_PATH + SAVED_SEARCH_RESOURCE))
        .build()));

    return repo.create(docToCreate, null).getDto().getJsonApiId();
  }

  @WithMockKeycloakUser(internalIdentifier="1d472bf2-514c-40af-9a60-77d6510a39fb", groupRole = {"aafc:USER"})
  @Transactional
  @Test
  void create_validResource_recordCreated()
    throws ResourceGoneException, ca.gc.aafc.dina.exception.ResourceNotFoundException {
    // Mock referential integrity to pass
    UUID expectedUserId = mockReferentialIntegrity(TEST_USER_ID, true);

    UUID savedId = persistUserPreferenceDto(expectedUserId);
    UserPreferenceDto result = repo.getOne(savedId, null).getDto();

    assertEquals(savedId, result.getUuid());
    assertEquals("value", result.getUiPreference().get("key"));
    assertEquals(TestResourceHelper.readContentAsJsonMap(TEST_RESOURCE_PATH +SAVED_SEARCH_RESOURCE), result.getSavedSearches());
    assertEquals(expectedUserId, result.getUserId());
    assertNotNull(result.getCreatedOn());

    //cleanup
    Assertions.assertDoesNotThrow(() -> repo.delete(savedId));
  }

  @WithMockKeycloakUser(internalIdentifier="1d472bf2-514c-40af-9a60-77d6510a39fb", groupRole = {"aafc:DINA_ADMIN"})
  @Test
  void create_WhenUserDoesNotExist_ThrowsBadRequest() {
    // Mock referential integrity to fail
    UUID userId = mockReferentialIntegrity(UUID.randomUUID(), false);

    assertThrows(IllegalArgumentException.class, () -> persistUserPreferenceDto(userId));
  }

  @WithMockKeycloakUser(internalIdentifier="1d472bf2-514c-40af-9a60-77d6510a39fb", groupRole = {"aafc:DINA_ADMIN"})
  @Transactional
  @Test
  void find_byUserID_recordFound() {
    // Mock referential integrity to pass for both.
    UUID expectedUserId1 = mockReferentialIntegrity(UUID.randomUUID(), true);
    UUID expectedUserId2 = mockReferentialIntegrity(UUID.randomUUID(), true);

    // Persist user preference dto records.
    UUID savedId1 = persistUserPreferenceDto(expectedUserId1);
    UUID savedId2 = persistUserPreferenceDto(expectedUserId2);

    // Search for a user preference for the expectedUserId1.
    QueryComponent qc = QueryComponent.builder().filters(new FilterExpression("userId",
      Ops.EQ, expectedUserId1.toString())
    ).build();

    DinaRepositoryV2.PagedResource<JsonApiDto<UserPreferenceDto>> resultList = repo.getAll(qc);

    // Ensure that the record with the expectedUserId1 UUID was brought back.
    assertEquals(1, resultList.totalCount());
    assertEquals(expectedUserId1, resultList.resourceList().getFirst().getDto().getUserId());

    //cleanup
    Assertions.assertDoesNotThrow(() -> repo.delete(savedId1));
    Assertions.assertDoesNotThrow(() -> repo.delete(savedId2));
  }

  @WithMockKeycloakUser(internalIdentifier="1d472bf2-514c-40af-9a60-77d6510a39fb", groupRole = {"aafc:USER"})
  @Transactional
  @Test
  void update_validUpdate_recordUpdated()
    throws ResourceGoneException, ca.gc.aafc.dina.exception.ResourceNotFoundException {
    // Mock referential integrity to pass
    UUID expectedUserId = mockReferentialIntegrity(TEST_USER_ID,true);

    // Create user preference record.
    UUID savedId = persistUserPreferenceDto(expectedUserId);

    // Retrieve the saved record and update it.
    UserPreferenceDto resultToUpdate = repo.getOne(savedId, null).getDto();
    resultToUpdate.setSavedSearches(TestResourceHelper.readContentAsJsonMap(TEST_RESOURCE_PATH +UPDATED_SAVED_SEARCH_RESOURCE));

    JsonApiDocument docToUpdate = JsonApiDocuments.createJsonApiDocument(
      savedId, UserPreferenceDto.TYPENAME,
      JsonAPITestHelper.toAttributeMap(resultToUpdate));
    Assertions.assertDoesNotThrow(() -> repo.update(docToUpdate));

    // Ensure the user preference has been updated.
    UserPreferenceDto updatedResult = repo.getOne(savedId, null).getDto();
    assertEquals(TestResourceHelper.readContentAsJsonMap(TEST_RESOURCE_PATH +UPDATED_SAVED_SEARCH_RESOURCE), updatedResult.getSavedSearches());

    Assertions.assertDoesNotThrow(() -> repo.delete(savedId));
  }

  @WithMockKeycloakUser(internalIdentifier="1d472bf2-514c-40af-9a60-77d6510a39fb", groupRole = {"aafc:USER"})
  @Transactional
  @Test
  void delete_existingRecord_recordDeleted() {
    // Mock referential integrity to pass
    UUID expectedUserId = mockReferentialIntegrity(TEST_USER_ID,true);

    // Create user preference record.
    UUID savedId = persistUserPreferenceDto(expectedUserId);

    // Delete the record and ensure it does not exist anymore.
    Assertions.assertDoesNotThrow(() -> repo.delete(savedId));
    assertThrows(ResourceNotFoundException.class, () -> repo.getOne(savedId, null).getDto());
  }
}
