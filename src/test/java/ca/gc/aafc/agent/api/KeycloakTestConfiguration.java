package ca.gc.aafc.agent.api;

import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.security.DinaRole;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Configuration class used to provide a test instance of {@link DinaAuthenticatedUser}. Keycloak
 * should be disabled in order to use the configuration.
 */
@TestConfiguration
public class KeycloakTestConfiguration {
  public static final String USER_NAME = "test_user";

  @Bean
  public DinaAuthenticatedUser dinaAuthenticatedUser() {
    return DinaAuthenticatedUser.builder()
      .username(USER_NAME)
      .rolesPerGroup(ImmutableMap.of("dev-group", Sets.newHashSet(DinaRole.STAFF)))
      .build();
  }
}