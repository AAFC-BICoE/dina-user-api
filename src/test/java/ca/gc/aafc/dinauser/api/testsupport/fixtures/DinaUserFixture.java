package ca.gc.aafc.dinauser.api.testsupport.fixtures;

import ca.gc.aafc.dina.security.DinaRole;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DinaUserFixture {

  public static final String STUDENT_ROLE = DinaRole.STUDENT.getKeycloakRoleName();

  public static DinaUserDto.DinaUserDtoBuilder newUserDto() {
    return DinaUserDto.builder()
        .agentId(UUID.randomUUID().toString())
        .username(RandomStringUtils.randomAlphabetic(5).toLowerCase())
        .firstName(RandomStringUtils.randomAlphabetic(5).toLowerCase())
        .lastName(RandomStringUtils.randomAlphabetic(5).toLowerCase())
        .emailAddress(RandomStringUtils.randomAlphabetic(5).toLowerCase() + "@user.com")
        .rolesPerGroup(Map.of("cnc", Set.of(STUDENT_ROLE)));
  }
}
