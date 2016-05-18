package com.fns.xlator;

import com.fns.xlator.cache.RedisCacheMonitor;
import com.fns.xlator.cache.RedisCacheStatisticsProvider;
import com.fns.xlator.client.api.TranslationService;
import com.fns.xlator.client.impl.frengly.FrenglyClientSettings;
import com.fns.xlator.client.impl.frengly.FrenglyTranslationService;
import com.fns.xlator.client.impl.google.GoogleClientSettings;
import com.fns.xlator.client.impl.google.GoogleTranslationService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.cache.ConcurrentMapCacheStatisticsProvider;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.dropwizard.DropwizardMetricServices;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.cloud.aws.cache.config.annotation.CacheClusterConfig;
import org.springframework.cloud.aws.cache.config.annotation.EnableElastiCache;
import org.springframework.cloud.aws.context.config.annotation.EnableContextCredentials;
import org.springframework.cloud.aws.context.config.annotation.EnableContextInstanceData;
import org.springframework.cloud.aws.context.config.annotation.EnableContextRegion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

// special thanks to https://github.com/spring-cloud-samples/aws-refapp/blob/master/src/main/java/org/springframework/cloud/aws/sample/ReferenceApplication.java

@Configuration
@ComponentScan
public class Application {

    public static final String CACHE_NAME = "translations";

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

    @Configuration
    @ConditionalOnProperty(prefix = "app.cache", name = "provider", havingValue = "local")
    @EnableCaching
    @EnableAutoConfiguration(exclude = { RedisAutoConfiguration.class })
    static class LocalCacheConfig {

        @Bean
        public CacheManager cacheManager() {
            final SimpleCacheManager cacheManager = new SimpleCacheManager();
            final List<Cache> caches = new ArrayList<>();
            caches.add(new ConcurrentMapCache(CACHE_NAME));
            cacheManager.setCaches(caches);
            return cacheManager;
        }

        @Bean
        public ConcurrentMapCacheStatisticsProvider concurrentMapCacheStatisticsProvider() {
            return new ConcurrentMapCacheStatisticsProvider();
        }

    }

    @Profile("aws")
    @Configuration
    @EnableContextCredentials(instanceProfile = true)
    @EnableContextRegion(autoDetect = true)
    @EnableContextInstanceData
    @EnableElastiCache({ @CacheClusterConfig(name = CACHE_NAME) })
    static class ElastiCacheConfig {

    }

    @Configuration
    @ConditionalOnProperty(prefix = "app.cache", name = "provider", havingValue = "redis")
    @EnableCaching
    @EnableAutoConfiguration
    static class SharedCacheConfig {

        @Bean
        public RedisTemplate<Object, Object> redisTemplate(
                RedisConnectionFactory redisConnectionFactory)
                throws UnknownHostException {
            RedisTemplate<Object, Object> template = new RedisTemplate<Object, Object>();
            template.setConnectionFactory(redisConnectionFactory);
            return template;
        }
        
        @Bean
        public RedisCacheManager cacheManager(RedisTemplate<Object, Object> redisTemplate) {
            RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
            List<String> cacheNames = new ArrayList<>();
            cacheNames.add(CACHE_NAME);
            cacheManager.setCacheNames(cacheNames);
            return cacheManager;
        }

        @Bean
        public RedisMessageListenerContainer redisMessageListenerContainer(
                RedisConnectionFactory redisConnectionFactory) {
            RedisMessageListenerContainer container = new RedisMessageListenerContainer();
            container.setConnectionFactory(redisConnectionFactory);
            return container;
        }

        @Bean
        public RedisCacheStatisticsProvider redisCacheStatisticsProvider(RedisConnectionFactory factory) {
            return new RedisCacheStatisticsProvider(factory);
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "app.cache", name = "provider", havingValue = "redis")
    @EnableScheduling
    static class CacheMonitorConfig {

        @Bean
        RedisCacheMonitor cacheMonitor(RedisConnectionFactory redisConnectionFactory,
                DropwizardMetricServices metricServices) {
            return new RedisCacheMonitor(redisConnectionFactory, metricServices);
        }
    }

    public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
