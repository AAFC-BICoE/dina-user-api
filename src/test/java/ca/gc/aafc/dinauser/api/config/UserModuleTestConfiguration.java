package ca.gc.aafc.dinauser.api.config;

import java.util.Properties;

import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class UserModuleTestConfiguration {

  @Bean
  public BuildProperties buildProperties() {
    Properties props = new Properties();
    props.setProperty("version", "test-user-module-version");
    return new BuildProperties(props);
  }

}
