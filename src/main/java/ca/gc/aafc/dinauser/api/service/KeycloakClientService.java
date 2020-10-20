package ca.gc.aafc.dinauser.api.service;

import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class KeycloakClientService {
  
  private static final String SECRET_PROPERTY_KEY = "secret";
  
  @Autowired
  private KeycloakSpringBootProperties keycloakProperties;
  
  @Bean
  @RequestScope
  public Keycloak getKeycloakClient() {
    //TODO make a config class for these properties
    final String serverUrl = keycloakProperties.getAuthServerUrl();
    final String realm = getRealm();
    final String clientId = keycloakProperties.getResource();
    final String secret = (String) keycloakProperties.getCredentials().get(SECRET_PROPERTY_KEY);
    
    // if possible, maybe have an ApplicationScoped builder with all the config values filled in
    // then generate the client with .authorization and .build
    return KeycloakBuilder.builder()
        .serverUrl(serverUrl)
        .realm(realm)
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .clientId(clientId)
        .clientSecret(secret)
        .authorization(getToken())
        .build();
  }
  
  public String getRealm() {
    return keycloakProperties.getRealm();
  }
  
  private String getToken() {
    log.info("getting user authentication token");
    KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) SecurityContextHolder.getContext()
        .getAuthentication();

    if (token == null) {
      log.error("no token");
      return null;
    }
    
    return token.getAccount()
        .getKeycloakSecurityContext()
        .getTokenString();
  }
  
}
