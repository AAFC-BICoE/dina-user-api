package ca.gc.aafc.dinauser.api.security;

import ca.gc.aafc.dina.security.DinaRole;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.dinauser.api.DinaUserModuleApiLauncher;
import ca.gc.aafc.dinauser.api.config.UserModuleTestConfiguration;
import ca.gc.aafc.dinauser.api.dto.DinaUserDto;

import io.crnk.core.exception.ForbiddenException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {UserModuleTestConfiguration.class, DinaUserModuleApiLauncher.class})
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@ContextConfiguration(initializers = PostgresTestContainerInitializer.class)
class UserAuthorizationServiceIT {

  @Autowired
  private UserAuthorizationService userAuthorizationService;

  @Test
  @WithMockKeycloakUser(internalIdentifier="1d472bf2-514c-40af-9a60-77d6510a39fb", groupRole = {"cnc:SUPER_USER"})
  void updateOwnRole_lowerRole_allowed() {
    DinaUserDto updateUser = createUserDto("1d472bf2-514c-40af-9a60-77d6510a39fb", DinaRole.USER);

    assertDoesNotThrow(() -> userAuthorizationService.authorizeUpdate(updateUser));
  }

  @Test
  @WithMockKeycloakUser(internalIdentifier="1d472bf2-514c-40af-9a60-77d6510a39fb", groupRole = {"cnc:SUPER_USER"})
  void updateOwnRole_sameRole_allowed() {
    DinaUserDto updateUser = createUserDto("1d472bf2-514c-40af-9a60-77d6510a39fb", DinaRole.SUPER_USER);

    assertDoesNotThrow(() -> userAuthorizationService.authorizeUpdate(updateUser));
  }

  @Test
  @WithMockKeycloakUser(internalIdentifier="1d472bf2-514c-40af-9a60-77d6510a39fb", groupRole = {"cnc:SUPER_USER"})
  void updateOwnRole_higherRole_forbidden() {
    DinaUserDto updateUser = createUserDto("1d472bf2-514c-40af-9a60-77d6510a39fb", DinaRole.DINA_ADMIN);

    assertThrows(ForbiddenException.class, () -> userAuthorizationService.authorizeUpdate(updateUser));
  }

  @Test
  @WithMockKeycloakUser(internalIdentifier="1d472bf2-514c-40af-9a60-77d6510a39fb", groupRole = {"cnc:SUPER_USER"})
  void updateAnotherUserRole_sameRole_forbidden() {
    DinaUserDto updateUser = createUserDto("internal-999", DinaRole.SUPER_USER); 

    assertThrows(ForbiddenException.class, () -> userAuthorizationService.authorizeUpdate(updateUser));
  }

  @Test
  @WithMockKeycloakUser(internalIdentifier="1d472bf2-514c-40af-9a60-77d6510a39fb", groupRole = {"cnc:SUPER_USER"})
  void updateAnotherUserRole_lowerRole_allowed() {
    DinaUserDto updateUser = createUserDto("internal-999", DinaRole.USER); 

    assertDoesNotThrow(() -> userAuthorizationService.authorizeUpdate(updateUser));
  }

  private DinaUserDto createUserDto(String internalId, DinaRole role) {
    Map<String, Set<String>> rolesPerGroup = new HashMap<>();
    rolesPerGroup.put("cnc", Set.of(role.getKeycloakRoleName()));

    DinaUserDto userDto = new DinaUserDto();
    userDto.setInternalId(internalId);
    userDto.setRolesPerGroup(rolesPerGroup);
    return userDto;
  }
}
