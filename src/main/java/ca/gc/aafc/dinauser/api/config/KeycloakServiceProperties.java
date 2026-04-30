package ca.gc.aafc.dinauser.api.config;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "keycloak", ignoreUnknownFields = false)
@Component
@Getter
@Setter
public class KeycloakServiceProperties {

  private Boolean enabled;

  private String authServerUrl;
  private String resource;
  private String realm;
  private Map<String, String> credentials;
}
