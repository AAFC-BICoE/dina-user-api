package ca.gc.aafc.dinauser.api.crudit;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dinauser.api.DinaUserModuleApiLauncher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Map;

@SpringBootTest(classes = DinaUserModuleApiLauncher.class)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
@Transactional
class UserPreferenceCRUDIT {

  @Inject
  private BaseDAO baseDAO;

  private DefaultDinaService<UserPreference> service;

  @BeforeEach
  void setUp() {
    service = new DefaultDinaService<>(baseDAO);
  }

  @Test
  void create() {
    UserPreference result = service.create(UserPreference.builder()
      .uiPreference(Map.of("key", "value"))
      .build());
    Assertions.assertNotNull(result.getId());
    Assertions.assertEquals("value", result.getUiPreference().get("key"));
  }
}
