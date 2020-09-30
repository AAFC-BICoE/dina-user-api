package ca.gc.aafc.dinauser.api;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dinauser.api.entities.DinaUser;
import ca.gc.aafc.dinauser.api.repository.DinaUserRepository;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/user")
@Log4j2
public class UserDemoController {

  @Autowired
  private Provider<DinaAuthenticatedUser> authUserProvider;
    
  @Autowired
  private DinaUserRepository userRepository;
  
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
  
  @GetMapping("list")
  public List<DinaUser> getUserList() {
    try {
      return userRepository.getUsers();
    } catch (RuntimeException e) {
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
