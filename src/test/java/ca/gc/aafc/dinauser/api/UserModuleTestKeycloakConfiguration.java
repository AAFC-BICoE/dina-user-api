package ca.gc.aafc.dinauser.api;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import ca.gc.aafc.dinauser.api.config.UserModuleTestConfiguration;

@TestConfiguration
@Import(UserModuleTestConfiguration.class)
public class UserModuleTestKeycloakConfiguration {
  
  public static final String KEYCLOAK_DOCKER_IMAGE = "quay.io/keycloak/keycloak:20.0.5";
  
  @Bean
  public Keycloak keycloakClient() {
    final DinaKeycloakTestContainer container = DinaKeycloakTestContainer.getInstance();
    return KeycloakBuilder.builder()
        .serverUrl(container.getAuthServerUrl())
        .realm("dina")
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .clientId("user-svc")
        .clientSecret("120c0b7a-5ed2-4295-9a31-29c2fcbc714f")
        .build();
  }
}
