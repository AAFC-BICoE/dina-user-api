package ca.gc.aafc.dinauser.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.testcontainers.junit.jupiter.Container;

import dasniko.testcontainers.keycloak.KeycloakContainer;

public class KeycloakTestContainerTest {
    private final String IMAGE_VERSION = UserModuleTestConfiguration.KEYCLOAK_DOCKER_IMAGE;
    private final String REALM_FILE_NAME = "keycloak-dina-starter-realm.json";
    private final String ADMIN_USER_NAME = "admin";
    private final String ADMIN_PASS_WORD = "admin";

    @Container
    private final KeycloakContainer keycloak = new KeycloakContainer(IMAGE_VERSION)
        .withRealmImportFile(REALM_FILE_NAME)
        .withAdminUsername(ADMIN_USER_NAME)
        .withAdminPassword(ADMIN_PASS_WORD);

    @BeforeEach
    public void before(){
        keycloak.start();
    }
     
    @AfterEach
    public void teardown(){
        keycloak.close();
    }

    @Test
    public void testContainerStarts(){
       assertNotNull(keycloak);
       assertTrue(keycloak.isRunning());
       assertNotNull(keycloak.getContainerId());
       assertEquals(IMAGE_VERSION, keycloak.getDockerImageName());
       assertEquals(ADMIN_USER_NAME, keycloak.getAdminUsername());
       assertEquals(ADMIN_PASS_WORD , keycloak.getAdminPassword());
    }

}
