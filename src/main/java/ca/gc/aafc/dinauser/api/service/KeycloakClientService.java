package ca.gc.aafc.dinauser.api.service;

import org.apache.commons.lang3.BooleanUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.ClientBuilder;
import lombok.extern.log4j.Log4j2;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

@Service
@Log4j2
public class KeycloakClientService {
  
  private static final String SECRET_PROPERTY_KEY = "secret";
  
  @Autowired
  private KeycloakSpringBootProperties keycloakProperties;

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
      final String secret = (String) keycloakProperties.getCredentials().get(SECRET_PROPERTY_KEY);

      ClientBuilder clientBuilder = ClientBuilder.newBuilder();
      if (clientBuilder instanceof ResteasyClientBuilder raClientBuilder) {
        raClientBuilder.connectionPoolSize(10);
      } else {
        log.debug("Not an instance of ResteasyClientBuilder, not setting connection pool size ");
      }

      if (BooleanUtils.isTrue(logKeycloakRequest)) {
        log.debug("enabling LogClientRequestFilter ");
        clientBuilder.register(new LogClientRequestFilter());
      }

      serviceClientBuilder = KeycloakBuilder.builder()
          .serverUrl(serverUrl)
          .realm(realm)
          .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
          .clientId(clientId)
          .clientSecret(secret)
          .resteasyClient(clientBuilder.build());
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
