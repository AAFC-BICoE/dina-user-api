package ca.gc.aafc.dinauser.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;

import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.server.ResponseStatusException;

import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/user")
@Log4j2
public class UserDemoController {

  @Autowired
  private Provider<DinaAuthenticatedUser> authUserProvider;
  
  @Autowired
  private KeycloakSpringBootProperties keycloakProperties;
  
  @Inject
  public UserDemoController() {
  }
  
  @GetMapping("hello")
  public String helloUser() {
    StringBuilder sb = new StringBuilder();
    
    sb.append("Hello ");
    
    final DinaAuthenticatedUser authenticatedUser = authUserProvider.get();
    
    if (authenticatedUser != null) {
      log.info("user: {} {} {}", authenticatedUser, authenticatedUser.getAgentIdentifer(), authenticatedUser.getUsername());
      sb.append(authenticatedUser.getUsername());
    } else {
      sb.append("world");
    }
    
    return sb.toString();
  }
  
  @Getter
  @Setter
  public class DemoUserInfo {
    String username;
    String firstName;
    String lastName;
    final List<String> roles;
    
    public DemoUserInfo() {
      roles = new ArrayList<String>();
    }
  }
  
  public String getToken() {
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
  
  @Bean
  @RequestScope
  public Keycloak getKeycloakClient() {
    log.info("creating keycloak client");
    
    final String serverUrl = keycloakProperties.getAuthServerUrl();
    final String realm = keycloakProperties.getRealm();
    final String clientId = keycloakProperties.getResource();
    final String secret = (String) keycloakProperties.getCredentials().get("secret");
    
    log.info("client settings: serverUrl {}, realm {}, clientId {}, secret {}", serverUrl, realm, clientId, secret);
    
    return KeycloakBuilder.builder()
        .serverUrl(serverUrl)
        .realm(realm)
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .clientId(clientId)
        .clientSecret(secret)
        .authorization(getToken())
        .build();
  }
  
  @GetMapping("list")
  public List<DemoUserInfo> getUserList() {
    log.info("getting user list");

    final Keycloak client = getKeycloakClient();

    log.info("got keycloak client");

    final List<DemoUserInfo> cookedUsers = new ArrayList<DemoUserInfo>();
    try {

      final List<UserRepresentation> rawUsers = client.realm(keycloakProperties.getRealm()).users().list();

      log.info("got raw users");

      for (final UserRepresentation userRepresentation : rawUsers) {
        final DemoUserInfo u = new DemoUserInfo();
        u.setFirstName(userRepresentation.getFirstName());
        u.setLastName(userRepresentation.getLastName());
        u.setUsername(userRepresentation.getUsername());
        if (userRepresentation.getRealmRoles() != null) {
          u.getRoles().addAll(userRepresentation.getRealmRoles());
        }
        cookedUsers.add(u);
      }

      return cookedUsers;
    } catch (RuntimeException e) {
      log.error("something went wrong", e);
      HttpStatus status = null;
      if (e instanceof WebApplicationException && ((WebApplicationException) e).getResponse() != null) {
        status = HttpStatus.resolve(((WebApplicationException) e).getResponse().getStatus());
      } else {
        status = HttpStatus.I_AM_A_TEAPOT;
      }
      throw new ResponseStatusException(status, "oops", e);
    }
  }

}
