/*-
 * #%L
 * xlator
 * %%
 * Copyright (C) 2016 - 2018 FNS
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.fns.xlator.client.config;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import com.fns.xlator.client.api.TranslationService;
import com.fns.xlator.client.impl.frengly.FrenglyClientSettings;
import com.fns.xlator.client.impl.frengly.FrenglyTranslationService;
import com.fns.xlator.client.impl.google.GoogleClientSettings;
import com.fns.xlator.client.impl.google.GoogleTranslationService;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;

@Configuration
public class AppConfig {

	public static final String CACHE_NAME = "translations";
	
    @Configuration
    static class StaticResourceConfiguration implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/docs/**")
                    .addResourceLocations("classpath:/public/")
                    .resourceChain(true)
                    .addResolver(new PathResourceResolver());
        }

        @Override
        public void addViewControllers(ViewControllerRegistry registry) {
            registry.addViewController("/docs")
                    .setViewName("forward:/docs/index.html");
        }
    }
    
    @Configuration
    static class MetricsConfig {
    
    	@Bean
    	public TimedAspect timedAspect(MeterRegistry registry) {
    		return new TimedAspect(registry);
    	}
    }
    
    @Configuration
    @ConditionalOnProperty(prefix = "app.defaults", name = "service", havingValue = "google")
    static class GoogleApiConfig {

        @Bean
        public GoogleClientSettings googleClientSettings() {
            return new GoogleClientSettings();
        }

        @Bean
        public TranslationService googleTranslationService(RestTemplate restTemplate, GoogleClientSettings settings) {
            return new GoogleTranslationService(restTemplate, settings);
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
        public TranslationService frenglyTranslationService(RestTemplate restTemplate, FrenglyClientSettings settings) {
            return new FrenglyTranslationService(restTemplate, settings);
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
        public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        	RedisCacheConfiguration config = RedisCacheConfiguration
        			.defaultCacheConfig()
        			.prefixKeysWith(CACHE_NAME)
        			.disableCachingNullValues();
        	return RedisCacheManagerBuilder
            		.fromConnectionFactory(connectionFactory)
            		.cacheDefaults(config)
            		.build();
        }

        @Bean
        public RedisMessageListenerContainer redisMessageListenerContainer(
                RedisConnectionFactory redisConnectionFactory) {
            RedisMessageListenerContainer container = new RedisMessageListenerContainer();
            container.setConnectionFactory(redisConnectionFactory);
            return container;
        }

    }
}
