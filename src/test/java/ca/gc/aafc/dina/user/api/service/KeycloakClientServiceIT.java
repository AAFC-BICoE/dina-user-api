package ca.gc.aafc.dina.user.api.service;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.springframework.test.util.ReflectionTestUtils;

import ca.gc.aafc.dina.user.api.DinaKeycloakTestContainer;
import ca.gc.aafc.dina.user.api.config.KeycloakServiceProperties;

/**
 * Exercises the production client built by {@link KeycloakClientService}, which the other tests
 * replace with their own Keycloak bean.
 */
public class KeycloakClientServiceIT {

  @Test
  void getKeycloakClient_canReadJsonFromAdminApi() {
    DinaKeycloakTestContainer container = DinaKeycloakTestContainer.getInstance();
    container.start();

    KeycloakServiceProperties props = new KeycloakServiceProperties();
    props.setAuthServerUrl(container.getAuthServerUrl());
    props.setRealm("dina");
    props.setResource("user-svc");
    props.setCredentials(Map.of("secret", "120c0b7a-5ed2-4295-9a31-29c2fcbc714f"));

    KeycloakClientService service = new KeycloakClientService();
    ReflectionTestUtils.setField(service, "keycloakProperties", props);
    ReflectionTestUtils.setField(service, "logKeycloakRequest", true);

    try (Keycloak keycloak = service.getKeycloakClient()) {
      // fetching the token and the user list both require a JSON MessageBodyReader
      assertFalse(keycloak.realm("dina").users().list(0, 1).isEmpty());
    }
  }
}
