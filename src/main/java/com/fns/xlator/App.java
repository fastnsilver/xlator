package com.fns.xlator;

import com.fns.xlator.client.api.TranslationService;
import com.fns.xlator.client.impl.frengly.FrenglyClientSettings;
import com.fns.xlator.client.impl.frengly.FrenglyTranslationService;
import com.fns.xlator.client.impl.google.GoogleClientSettings;
import com.fns.xlator.client.impl.google.GoogleTranslationService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.cloud.aws.autoconfigure.cache.ElastiCacheAutoConfiguration;
import org.springframework.cloud.aws.cache.config.annotation.CacheClusterConfig;
import org.springframework.cloud.aws.cache.config.annotation.EnableElastiCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

// special thanks to https://github.com/spring-cloud-samples/aws-refapp/blob/master/src/main/java/org/springframework/cloud/aws/sample/ReferenceApplication.java

@SpringBootApplication(exclude = ElastiCacheAutoConfiguration.class)
public class App {

    @Configuration
    @ConditionalOnProperty(prefix = "app.defaults", name = "service", havingValue = "google")
    static class GoogleApiConfig {

        @Bean
        public GoogleClientSettings googleClientSettings() {
            return new GoogleClientSettings();
        }

        @Bean
        public TranslationService googleTranslationService(RestTemplate restTemplate, GoogleClientSettings settings,
                CounterService counterService) {
            return new GoogleTranslationService(restTemplate, settings, counterService);
        }

    }

    @Configuration
    @ConditionalOnProperty(prefix = "app.defaults", name = "service", havingValue = "frengly")
    static class FrenglyApiConfig {

        @Bean
        public FrenglyClientSettings frenglyClientSettings() {
            return new FrenglyClientSettings();
        }

        @Bean
        public TranslationService frenglyTranslationService(RestTemplate restTemplate, FrenglyClientSettings settings,
                CounterService counterService) {
            return new FrenglyTranslationService(restTemplate, settings, counterService);
        }
    }

    @Profile("!aws")
    @Configuration
    static class LocalCacheConfig {

        @Bean
        public CacheManager createSimpleCacheManager() {
            SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
            List<Cache> caches = new ArrayList<>();
            caches.add(new ConcurrentMapCache("translations"));
            simpleCacheManager.setCaches(caches);
            return simpleCacheManager;
        }

    }

    @Profile("aws")
    @Configuration
    @EnableElastiCache({ @CacheClusterConfig(name = "translations" /*, expiration = 1209600 */) })
    static class ElastiCacheConfig {

    }


	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}
