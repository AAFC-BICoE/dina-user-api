package ca.gc.aafc.dinauser.api.repository;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import ca.gc.aafc.dinauser.api.entities.DinaUser;
import ca.gc.aafc.dinauser.api.service.KeycloakClientService;
import lombok.extern.log4j.Log4j2;

@Repository
@Log4j2
public class DinaUserRepository {
  
  @Autowired
  private KeycloakClientService keycloakClientService;
  
  @Autowired
  private Keycloak keycloakClient;
  
  private UsersResource getUsersResource() {
    return keycloakClient.realm(keycloakClientService.getRealm()).users();
  }
  
  private DinaUser convertFromRaw(UserRepresentation rawUser) {
    final DinaUser user = new DinaUser();
    
    user.setUsername(rawUser.getUsername());
    user.setFirstName(rawUser.getFirstName());
    user.setLastName(rawUser.getLastName());
    user.setEmailAddress(rawUser.getEmail());
    
    //TODO agentId and roles
    
    return user;
  }
  
  public List<DinaUser> getUsers() {
    final List<DinaUser> cookedUsers = new ArrayList<DinaUser>();
    
    log.info("getting raw user list");
    final List<UserRepresentation> rawUsers = getUsersResource().list();
    
    log.info("converting users");
    for (final UserRepresentation rawUser : rawUsers) {
      cookedUsers.add(convertFromRaw(rawUser));
    }
    
    log.info("done converting users; returning");
    return cookedUsers;
  }

}
