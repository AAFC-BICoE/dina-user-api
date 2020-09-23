package ca.gc.aafc.dinauser.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Launches the application.
 */
// CHECKSTYLE:OFF HideUtilityClassConstructor (Configuration class can not have
// invisible constructor, ignore the check style error for this case)
@SpringBootApplication
public class DinaUserModuleApiLauncher {
  public static void main(String[] args) {
    SpringApplication.run(DinaUserModuleApiLauncher.class, args);
  }
}
