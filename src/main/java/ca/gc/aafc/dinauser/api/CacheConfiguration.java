package ca.gc.aafc.dinauser.api;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfiguration {
  
  @Bean
  public Caffeine<Object,Object> caffeineConfig() {
    return Caffeine.newBuilder();
  }

  @Bean
  public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
    CaffeineCacheManager ccm = new CaffeineCacheManager();
    ccm.setCaffeine(caffeine);
    return ccm;
  }
}
