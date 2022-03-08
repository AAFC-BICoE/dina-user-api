package ca.gc.aafc.dinauser.api.repository;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dinauser.api.DinaUserModuleApiLauncher;
import ca.gc.aafc.dinauser.api.TestResourceHelper;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.dto.UserPreferenceDto;
import ca.gc.aafc.dinauser.api.service.DinaUserService;
import ca.gc.aafc.dinauser.api.testsupport.fixtures.UserPreferenceFixture;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

@SpringBootTest(classes = DinaUserModuleApiLauncher.class)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@ContextConfiguration(initializers = PostgresTestContainerInitializer.class)
public class UserPreferenceRepositoryIT {

  public static final String TEST_RESOURCE_PATH = "src/test/resources/test-documents/";
  public static final String SAVED_SEARCH_RESOURCE = "user-preference-saved-search-data.json";
  public static final String UPDATED_SAVED_SEARCH_RESOURCE = "user-preference-saved-search-data-updated.json";

  private final QuerySpec querySpec = new QuerySpec(UserPreferenceDto.class);

  @Inject
  private UserPreferenceRepository repo;

  @MockBean
  private DinaUserService userService;

  private UUID mockReferentialIntegrity(boolean returnValue) {
    UUID expectedUserId = UUID.randomUUID();
    Mockito.when(userService.exists(DinaUserDto.class, expectedUserId.toString())).thenReturn(returnValue);    
    return expectedUserId;
  }

  private UUID persistUserPreferenceDto(UUID expectedUserId) {
    return repo.create(UserPreferenceFixture.newUserPreferenceDto(expectedUserId)
        .savedSearches(TestResourceHelper.readContentAsJsonMap(TEST_RESOURCE_PATH + SAVED_SEARCH_RESOURCE))
        .build()).getUuid();
  }

  @Test
  void create_validResource_recordCreated() {
    // Mock referential integrity to pass
    UUID expectedUserId = mockReferentialIntegrity(true);

    UUID savedId = persistUserPreferenceDto(expectedUserId);
    UserPreferenceDto result = repo.findOne(savedId, querySpec);

    Assertions.assertEquals(savedId, result.getUuid());
    Assertions.assertEquals("value", result.getUiPreference().get("key"));
    Assertions.assertEquals(TestResourceHelper.readContentAsJsonMap(TEST_RESOURCE_PATH +SAVED_SEARCH_RESOURCE), result.getSavedSearches());
    Assertions.assertEquals(expectedUserId, result.getUserId());
    Assertions.assertNotNull(result.getCreatedOn());
  }

  @Test
  void create_WhenUserDoesNotExist_ThrowsBadRequest() {
    // Mock referential integrity to fail
    UUID userId = mockReferentialIntegrity(false);

    Assertions.assertThrows(BadRequestException.class, () -> persistUserPreferenceDto(userId));
  }

  @Test
  void find_byUserID_recordFound() {
    // Mock referential integrity to pass for both.
    UUID expectedUserId1 = mockReferentialIntegrity(true);
    UUID expectedUserId2 = mockReferentialIntegrity(true);

    // Persist user preference dto records.
    persistUserPreferenceDto(expectedUserId1);
    persistUserPreferenceDto(expectedUserId2);

    // Search for a user preference for the expectedUserId1.
    QuerySpec customQuery = new QuerySpec(UserPreferenceDto.class);
    customQuery.addFilter(PathSpec.of("userId").filter(FilterOperator.EQ, expectedUserId1.toString()));
    List<UserPreferenceDto> resultList = repo.findAll(null, customQuery);

    // Ensure that the record with the expectedUserId1 UUID was brought back.
    Assertions.assertEquals(1, resultList.size());
    Assertions.assertEquals(expectedUserId1, resultList.get(0).getUserId());
  }

  @Test
  void update_validUpdate_recordUpdated() {
    // Mock referential integrity to pass
    UUID expectedUserId = mockReferentialIntegrity(true);

    // Create user preference record.
    UUID savedId = persistUserPreferenceDto(expectedUserId);

    // Retrieve the saved record and update it.
    UserPreferenceDto resultToUpdate = repo.findOne(savedId, querySpec);
    resultToUpdate.setSavedSearches(TestResourceHelper.readContentAsJsonMap(TEST_RESOURCE_PATH +UPDATED_SAVED_SEARCH_RESOURCE));
    Assertions.assertDoesNotThrow(() -> repo.save(resultToUpdate));

    // Ensure the user preference has been updated.
    UserPreferenceDto updatedResult = repo.findOne(savedId, querySpec);
    Assertions.assertEquals(TestResourceHelper.readContentAsJsonMap(TEST_RESOURCE_PATH +UPDATED_SAVED_SEARCH_RESOURCE), updatedResult.getSavedSearches());
  }

  @Test
  void delete_existingRecord_recordDeleted() {
    // Mock referential integrity to pass
    UUID expectedUserId = mockReferentialIntegrity(true);

    // Create user preference record.
    UUID savedId = persistUserPreferenceDto(expectedUserId);

    // Delete the record and ensure it does not exist anymore.
    Assertions.assertDoesNotThrow(() -> repo.delete(savedId));
    Assertions.assertThrows(ResourceNotFoundException.class, () -> repo.findOne(savedId, querySpec));
  }
}
