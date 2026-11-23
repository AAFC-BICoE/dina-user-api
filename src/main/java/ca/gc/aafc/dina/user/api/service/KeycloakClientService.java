package ca.gc.aafc.dina.user.api.service;

import org.apache.commons.lang3.BooleanUtils;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.client.Client;
import lombok.extern.log4j.Log4j2;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

import ca.gc.aafc.dina.user.api.config.KeycloakServiceProperties;

@Service
@ConditionalOnProperty(value = "keycloak.enabled", matchIfMissing = true)
@Log4j2
public class KeycloakClientService {
  
  private static final String SECRET_PROPERTY_KEY = "secret";
  
  @Autowired
  private KeycloakServiceProperties keycloakProperties;

  @Value("${dina.userapi.keycloak.logRequest:false}")
  private Boolean logKeycloakRequest;
  
  private KeycloakBuilder serviceClientBuilder;
  
  @Bean
  public Keycloak getKeycloakClient() {
    return getKeycloakBuilder().build();
  }
  
  private KeycloakBuilder getKeycloakBuilder() {
    if (serviceClientBuilder == null) {
      log.debug("creating keycloak service client builder");
      final String serverUrl = keycloakProperties.getAuthServerUrl();
      final String realm = getRealm();
      final String clientId = keycloakProperties.getResource();
      final String secret = keycloakProperties.getCredentials().get(SECRET_PROPERTY_KEY);

      // Use Keycloak's client provider (Jackson 3) so the JSON providers it needs are registered.
      // It also configures a connection pool of 10.
      Client client = Keycloak.getClientProvider().newRestEasyClient(null, null, false);

      if (BooleanUtils.isTrue(logKeycloakRequest)) {
        log.debug("enabling LogClientRequestFilter ");
        client.register(new LogClientRequestFilter());
      }

      serviceClientBuilder = KeycloakBuilder.builder()
          .serverUrl(serverUrl)
          .realm(realm)
          .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
          .clientId(clientId)
          .clientSecret(secret)
          .resteasyClient(client);
    }

    log.debug("returning keycloak service client builder");
    
    return serviceClientBuilder;
  }
  
  public String getRealm() {
    return keycloakProperties.getRealm();
  }

  public static class LogClientRequestFilter implements ClientRequestFilter {
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
      log.info(requestContext.getUri().toString());
    }
  }

}
