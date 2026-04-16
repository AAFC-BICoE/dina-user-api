package ca.gc.aafc.dinauser.api;

import ca.gc.aafc.dina.DinaBaseApiAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.toedter.spring.hateoas.jsonapi.JsonApiConfiguration;

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
      .withObjectMapperCustomizer(objectMapper -> {
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.registerModule(new JavaTimeModule());
      });
  }

}
