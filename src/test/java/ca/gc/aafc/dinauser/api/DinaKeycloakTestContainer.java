package ca.gc.aafc.dinauser.api;

import dasniko.testcontainers.keycloak.KeycloakContainer;

public class DinaKeycloakTestContainer extends KeycloakContainer {
    private static final String IMAGE_VERSION = UserModuleTestConfiguration.KEYCLOAK_DOCKER_IMAGE;
    private static final String REALM_FILE_NAME = "keycloak-dina-starter-realm.json";
    private static final String ADMIN_USER_NAME = "admin";
    private static final String ADMIN_PASS_WORD = "admin";    
    private static DinaKeycloakTestContainer container;
 
    private DinaKeycloakTestContainer() {
        super(IMAGE_VERSION);
    }
 
    public static DinaKeycloakTestContainer getInstance() {
        if (container == null) {
            container = new DinaKeycloakTestContainer();
        }
        return container;
    }
 
    @Override
    public void start() {
        container.withRealmImportFile(REALM_FILE_NAME)
        .withAdminUsername(ADMIN_USER_NAME)  
        .withAdminPassword(ADMIN_PASS_WORD);
        super.start();
    }
 
    @Override
    public void stop() {
        //do nothing, JVM handles shut down
    }    
    
}
