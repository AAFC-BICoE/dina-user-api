package ca.gc.aafc.dinauser.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Configuration;

import ca.gc.aafc.dina.dto.ApiInfoDto;

@Configuration
public class ApiInfoConfiguration {

  @Value("${dina.messaging.isConsumer:false}")
  private Boolean isConsumer;

  private final String apiVersion;

  public ApiInfoConfiguration(BuildProperties buildProperties) {
    this.apiVersion = buildProperties.getVersion();
  }

  public ApiInfoDto buildApiInfoDto() {
    ApiInfoDto infoDto = new ApiInfoDto();
    infoDto.setModuleVersion(apiVersion);
    infoDto.setMessageConsumer(isConsumer);
    return infoDto;
  }
}
