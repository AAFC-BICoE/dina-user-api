package ca.gc.aafc.dinauser.api;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import ca.gc.aafc.dinauser.api.service.DinaUserService;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/api/v1/users")
@Log4j2
public class DinaUserRepository {
    
  @Autowired
  private DinaUserService userRepository;
  
  @Inject
  public DinaUserRepository() {
  }
  
  @GetMapping("")
  public List<DinaUserDto> getUserList() {
    log.debug("requested user list");
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
  
  @GetMapping("/{id}")
  public DinaUserDto getUser(@PathVariable final String id) {
    log.debug("requested user '{}'", id);
    try {
      return userRepository.getUser(id);
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
  
  @PostMapping("")
  public void createUser(@RequestBody final DinaUserDto user) {
    if (user == null) {
      log.error("cannot create null user");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    
    log.debug("creating new user: {}", user.getUsername());
    
    userRepository.createUser(user);
  }
  
  @PutMapping("/{id}")
  public void updateUser(@PathVariable final String id,
      @RequestBody final DinaUserDto user) {
    if (user == null) {
      log.error("cannot update null user");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot update null user");
    } else if (user.getInternalId() == null || !user.getInternalId().equals(id)) {
      log.error("mismatch: id {}, user {}", id, user.getInternalId());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user and id don't match");
    }
    
    log.debug("updating user {}", id);
    
    userRepository.updateUser(user);
  }
  
  @DeleteMapping("/{id}")
  public void deleteUser(@PathVariable final String id) {
    log.debug("deleting user {}", id);
    userRepository.deleteUser(id);
  }
  
}
