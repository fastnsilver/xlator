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
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableCaching
public class App {

    @Configuration
    @ConditionalOnProperty(prefix = "app.defaults", name = "service", havingValue = "google")
    static class GoogleApi {

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
    static class FrenglyApi {

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


	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}
