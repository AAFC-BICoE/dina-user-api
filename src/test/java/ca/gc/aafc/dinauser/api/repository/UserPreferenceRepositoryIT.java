package ca.gc.aafc.dinauser.api.repository;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dinauser.api.DinaUserModuleApiLauncher;
import ca.gc.aafc.dinauser.api.dto.UserPreferenceDto;
import io.crnk.core.queryspec.QuerySpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.inject.Inject;
import java.util.Map;

@SpringBootTest(classes = DinaUserModuleApiLauncher.class)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
class UserPreferenceRepositoryIT {

  @Inject
  private UserPreferenceRepository repo;

  @Test
  void create() {
    Integer id = repo.create(UserPreferenceDto.builder()
      .uiPreference(Map.of("key","value"))
      .build()).getId();
    UserPreferenceDto result = repo.findOne(id, new QuerySpec(UserPreferenceDto.class));
    Assertions.assertEquals(id, result.getId());
    Assertions.assertEquals("value", result.getUiPreference().get("key"));
  }
}
