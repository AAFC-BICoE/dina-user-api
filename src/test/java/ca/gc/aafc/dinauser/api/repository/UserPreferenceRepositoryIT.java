package ca.gc.aafc.dinauser.api.repository;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dinauser.api.DinaUserModuleApiLauncher;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.dto.UserPreferenceDto;
import ca.gc.aafc.dinauser.api.service.DinaUserService;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.inject.Inject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SpringBootTest(classes = DinaUserModuleApiLauncher.class)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@ContextConfiguration(initializers = PostgresTestContainerInitializer.class)
class UserPreferenceRepositoryIT {

  private static final String TEST_RESOURCE_PATH = "src/test/resources/test-documents/";
  private static final String SAVED_SEARCH_RESOURCE = "user-preference-saved-search-data.json";
  private static final String UPDATED_SAVED_SEARCH_RESOURCE = "user-preference-saved-search-data-updated.json";

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final QuerySpec querySpec = new QuerySpec(UserPreferenceDto.class);

  @Inject
  private UserPreferenceRepository repo;

  @MockBean
  private DinaUserService userService;

  private Map<String, Object> fileToJsonMap(String fileName) {
    try {
      Path filename = Path.of(TEST_RESOURCE_PATH + fileName);
      String jsonString = Files.readString(filename);

      return objectMapper.readValue(jsonString, new TypeReference<HashMap<String, Object>>() {});
    } catch (IOException e) {
      Assertions.fail("Unable to read saved search data file from test resources.");
    }

    return null;
  }

  private UUID mockReferentialIntegrity(boolean returnValue) {
    UUID expectedUserId = UUID.randomUUID();
    Mockito.when(userService.exists(DinaUserDto.class, expectedUserId.toString())).thenReturn(returnValue);    
    return expectedUserId;
  }

  private UUID persistPerferenceDto(UUID expectedUserId) {
    return repo.create(UserPreferenceDto.builder()
        .uiPreference(Map.of("key", "value"))
        .savedSearches(fileToJsonMap(SAVED_SEARCH_RESOURCE))
        .userId(expectedUserId)
        .build()).getUuid();
  }

  @Test
  void create_recordCreated() {
    // Mock referential integrity to pass
    UUID expectedUserId = mockReferentialIntegrity(true);

    UUID savedId = persistPerferenceDto(expectedUserId);
    UserPreferenceDto result = repo.findOne(savedId, querySpec);

    Assertions.assertEquals(savedId, result.getUuid());
    Assertions.assertEquals("value", result.getUiPreference().get("key"));
    Assertions.assertEquals(fileToJsonMap(SAVED_SEARCH_RESOURCE), result.getSavedSearches());
    Assertions.assertEquals(expectedUserId, result.getUserId());
    Assertions.assertNotNull(result.getCreatedOn());
  }

  @Test
  void create_WhenUserDoesNotExist_ThrowsBadRequest() {
    // Mock referential integrity to fail
    UUID userId = mockReferentialIntegrity(false);

    Assertions.assertThrows(BadRequestException.class, () -> persistPerferenceDto(userId));
  }

  @Test
  void update_recordUpdated() {
    // Mock referential integrity to pass
    UUID expectedUserId = mockReferentialIntegrity(true);

    // Create user preference record.
    UUID savedId = persistPerferenceDto(expectedUserId);

    // Retrieve the saved record and update it.
    UserPreferenceDto resultToUpdate = repo.findOne(savedId, querySpec);
    resultToUpdate.setSavedSearches(fileToJsonMap(UPDATED_SAVED_SEARCH_RESOURCE));
    Assertions.assertDoesNotThrow(() -> repo.save(resultToUpdate));

    // Ensure the user preference has been updated.
    UserPreferenceDto updatedResult = repo.findOne(savedId, querySpec);
    Assertions.assertEquals(fileToJsonMap(UPDATED_SAVED_SEARCH_RESOURCE), updatedResult.getSavedSearches());
  }

  @Test
  void delete_recordDeleted() {
    // Mock referential integrity to pass
    UUID expectedUserId = mockReferentialIntegrity(true);

    // Create user preference record.
    UUID savedId = persistPerferenceDto(expectedUserId);

    // Delete the record and ensure it does not exist anymore.
    Assertions.assertDoesNotThrow(() -> repo.delete(savedId));
    Assertions.assertThrows(ResourceNotFoundException.class, () -> repo.findOne(savedId, querySpec));
  }
}
