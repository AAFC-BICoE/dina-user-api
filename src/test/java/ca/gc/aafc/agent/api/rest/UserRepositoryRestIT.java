package ca.gc.aafc.agent.api.rest;

import ca.gc.aafc.dina.testsupport.BaseRestAssuredTest;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dinauser.api.DinaUserModuleApiLauncher;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.repository.UserRepository;
import io.crnk.core.queryspec.QuerySpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.inject.Inject;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = DinaUserModuleApiLauncher.class
)
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class})
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
public class UserRepositoryRestIT extends BaseRestAssuredTest {
  protected UserRepositoryRestIT() {
    super("");
  }

  @Inject
  private UserRepository repository;

  @Test
  void findAll() {
    Assertions.assertNull(repository.findAll(new QuerySpec(DinaUserDto.class)));
  }
}
