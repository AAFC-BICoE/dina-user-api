package ca.gc.aafc.dinauser.api.repository;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.dinauser.api.DinaUserModuleApiLauncher;
import ca.gc.aafc.dinauser.api.config.UserModuleTestConfiguration;
import ca.gc.aafc.dinauser.api.dto.DinaGroupDto;

/**
 * Test using dev-user and no Keycloak.
 */
@SpringBootTest(classes = {UserModuleTestConfiguration.class, DinaUserModuleApiLauncher.class})
@TestPropertySource(properties = {"keycloak.enabled=false","dev-user.enabled=true","spring.config.additional-location=classpath:application-test.yml"})
@ContextConfiguration(initializers = PostgresTestContainerInitializer.class)
public class GroupRepositoryIT {

  @Inject
  private GroupRepository groupRepository;

  /**
   *
   */
  @WithMockKeycloakUser(internalIdentifier="1d472bf2-514c-40af-9a60-77d6510a39fb", adminRole = {"DINA-ADMIN"})
  @Test
  void create_validResource_recordCreated() {
    DinaGroupDto dto = DinaGroupDto.builder()
      .name("my-new-group")
      .label("en", "my new group")
      .label("fr", "mon nouveau groupe")
      .build();
    groupRepository.create(dto);
  }
}
