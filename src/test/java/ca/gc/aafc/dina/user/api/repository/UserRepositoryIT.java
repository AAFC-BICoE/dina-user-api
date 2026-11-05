package ca.gc.aafc.dina.user.api.repository;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.dina.user.api.DinaUserModuleApiLauncher;
import ca.gc.aafc.dina.user.api.config.UserModuleTestConfiguration;
import ca.gc.aafc.dina.user.api.dto.DinaUserDto;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test using dev-user and no Keycloak.
 */
@SpringBootTest(classes = {UserModuleTestConfiguration.class, DinaUserModuleApiLauncher.class})
@TestPropertySource(properties = {"keycloak.enabled=false","dev-user.enabled=true","spring.config.additional-location=classpath:application-test.yml"})
@ContextConfiguration(initializers = PostgresTestContainerInitializer.class)
public class UserRepositoryIT {

  @Inject
  private WebApplicationContext wac;

  @Value("${dina.apiPrefix:}")
  private String apiPrefix;

  @WithMockKeycloakUser(internalIdentifier="1d472bf2-514c-40af-9a60-77d6510a39fb", adminRole = {"DINA-ADMIN"})
  @Test
  void onFindAll_pageOffsetLargerThanMaxInt_badRequest() throws Exception {
    MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

    mockMvc.perform(
        get(apiPrefix + "/" + DinaUserDto.TYPENAME + "?page[offset]=" + ((long) Integer.MAX_VALUE + 1))
          .contentType(JSON_API_VALUE))
      .andExpect(status().isBadRequest());
  }
}
