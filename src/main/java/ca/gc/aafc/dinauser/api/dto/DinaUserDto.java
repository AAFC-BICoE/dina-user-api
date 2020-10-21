package ca.gc.aafc.dinauser.api.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DinaUserDto {
  
  private String internalId;
  
  private String username;
  private String firstName;
  private String lastName;
  private String agentId;
  private String emailAddress;
  private final List<String> roles = new ArrayList<String>();
  private final List<String> groups = new ArrayList<String>();
  
}
