package ca.gc.aafc.dina.user.api;

import ca.gc.aafc.dina.DinaBaseApiAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.toedter.spring.hateoas.jsonapi.JsonApiConfiguration;
import tools.jackson.databind.cfg.DateTimeFeature;

@Configuration
@ComponentScan(basePackageClasses = DinaBaseApiAutoConfiguration.class)
@ImportAutoConfiguration(DinaBaseApiAutoConfiguration.class)
@EnableScheduling
public class MainConfiguration {

  @Bean
  public JsonApiConfiguration jsonApiConfiguration() {
    return new JsonApiConfiguration()
      .withPluralizedTypeRendered(false)
      .withPageMetaAutomaticallyCreated(false)
      .withMapperCustomizer(builder -> builder.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS));
  }

}
