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

    @Container
    private KeycloakContainer keycloak = new KeycloakContainer("jboss/keycloak:11.0.2")
    .withRealmImportFile("keycloak-dina-starter-realm.json")
    .withAdminUsername("admin")  
    .withAdminPassword("admin")
    .withExposedPorts(80);

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
       assertEquals( "jboss/keycloak:11.0.2", keycloak.getDockerImageName());
       assertEquals("admin" , keycloak.getAdminUsername());
       assertEquals("password" , keycloak.getAdminPassword());
    }

}