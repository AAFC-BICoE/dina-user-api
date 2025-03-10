package ca.gc.aafc.dinauser.api.crudit;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dinauser.api.DinaUserModuleApiLauncher;
import ca.gc.aafc.dinauser.api.entity.UserPreference;
import ca.gc.aafc.dinauser.api.service.DinaUserService;
import ca.gc.aafc.dinauser.api.service.UserPreferenceService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Map;
import java.util.UUID;

@SpringBootTest(classes = DinaUserModuleApiLauncher.class)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
@Transactional
class UserPreferenceCRUDIT {

  @Inject
  private UserPreferenceService service;

  @MockBean
  private DinaUserService userService;

  @BeforeEach
  void setUp() {
    // Mock referential integrity to pass
    Mockito.when(userService.exists(ArgumentMatchers.any()))
      .thenReturn(true);
  }

  @Test
  void create() {
    UUID expectedUserId = UUID.randomUUID();
    UserPreference result = service.create(UserPreference.builder()
      .uiPreference(Map.of("key", "value"))
      .userId(expectedUserId)
      .build());
    Assertions.assertNotNull(result.getId());
    Assertions.assertEquals("value", result.getUiPreference().get("key"));
    Assertions.assertEquals(expectedUserId, result.getUserId());
    Assertions.assertNotNull(result.getCreatedOn());

    //cleanup
    service.delete(result);
  }
}
