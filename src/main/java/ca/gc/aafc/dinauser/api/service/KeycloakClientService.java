package ca.gc.aafc.dinauser.api.service;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class KeycloakClientService {
  
  private static final String SECRET_PROPERTY_KEY = "secret";
  
  @Autowired
  private KeycloakSpringBootProperties keycloakProperties;
  
  private KeycloakBuilder serviceClientBuilder;
  
  @Bean
  @RequestScope
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
      
      serviceClientBuilder = KeycloakBuilder.builder()
          .serverUrl(serverUrl)
          .realm(realm)
          .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
          .clientId(clientId)
          .clientSecret(secret)
          .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build());
    }
    
    log.debug("returning keycloak service client builder");
    
    return serviceClientBuilder;
  }
  
  public String getRealm() {
    return keycloakProperties.getRealm();
  }
  
}
