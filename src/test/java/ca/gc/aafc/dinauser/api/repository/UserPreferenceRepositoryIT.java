package ca.gc.aafc.dinauser.api.repository;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dinauser.api.DinaUserModuleApiLauncher;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.dto.UserPreferenceDto;
import ca.gc.aafc.dinauser.api.service.DinaUserService;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.queryspec.QuerySpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

@SpringBootTest(classes = DinaUserModuleApiLauncher.class)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
class UserPreferenceRepositoryIT {

  @Inject
  private UserPreferenceRepository repo;

  @MockBean
  private DinaUserService userService;

  @BeforeEach
  void setUp() {
  }

  @Test
  void create() {
    UUID expectedUserId = UUID.randomUUID();
    // Mock referential integrity to pass
    Mockito.when(userService.exists(DinaUserDto.class, expectedUserId.toString())).thenReturn(true);

    Integer id = repo.create(UserPreferenceDto.builder()
      .uiPreference(Map.of("key", "value"))
      .userId(expectedUserId.toString())
      .build()).getId();
    UserPreferenceDto result = repo.findOne(id, new QuerySpec(UserPreferenceDto.class));
    Assertions.assertEquals(id, result.getId());
    Assertions.assertEquals("value", result.getUiPreference().get("key"));
    Assertions.assertEquals(expectedUserId.toString(), result.getUserId());
    Assertions.assertNotNull(result.getCreatedOn());
  }

  @Test
  void create_WhenUserDoesNotExist_ThrowsBadRequest() {
    UUID userId = UUID.randomUUID();
    // Mock referential integrity to fail
    Mockito.when(userService.exists(DinaUserDto.class, userId.toString())).thenReturn(false);

    Assertions.assertThrows(BadRequestException.class, () -> repo.create(UserPreferenceDto.builder()
      .uiPreference(Map.of("key", "value"))
      .userId(userId.toString())
      .build()));
  }
}
