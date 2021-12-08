package ca.gc.aafc.dinauser.api;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

@TestConfiguration
public class UserModuleTestConfiguration {
  
  public static final String KEYCLOAK_DOCKER_IMAGE = "jboss/keycloak:12.0.4";
  
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

  @Bean
  public BuildProperties buildProperties() {
    Properties props = new Properties();
    props.setProperty("version", "test-user-module-version");
    return new BuildProperties(props);
  }
}
