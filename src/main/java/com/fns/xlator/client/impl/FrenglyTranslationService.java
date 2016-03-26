package com.fns.xlator.client.impl;

import com.fns.xlator.client.api.TranslationService;
import com.fns.xlator.model.Translation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;

@Service
public class FrenglyTranslationService implements TranslationService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private RestTemplate restTemplate;
    private FrenglyClientSettings settings;
    private CounterService counterService;

    @Autowired
    public FrenglyTranslationService(RestTemplate restTemplate, FrenglyClientSettings settings,
            CounterService counterService) {
        this.restTemplate = restTemplate;
        this.settings = settings;
        this.counterService = counterService;
    }

    private Locale parseLocale(String locale) {
        String[] parts = locale.split("_");
        switch (parts.length) {
            case 3:
                return new Locale(parts[0], parts[1], parts[2]);
            case 2:
                return new Locale(parts[0], parts[1]);
            case 1:
                return new Locale(parts[0]);
            default:
                throw new IllegalArgumentException("Invalid locale: " + locale);
        }
    }

    private boolean isValidLocale(String code) {
        try {
            Locale locale = parseLocale(code);
            return Arrays.asList(Locale.getAvailableLocales()).contains(locale);
        } catch (MissingResourceException e) {
            return false;
        }
    }

    protected void validateTranslationArguments(String source, String target, String text) {
        Assert.hasText(target, "[Assertion failed] - Target locale code must not be null, empty or blank!");
        Assert.hasText(text, "[Assertion failed] - Text to translate must not be null, empty or blank!");
        Assert.isTrue(isValidLocale(source), String.format("[Assertion failed] - Source [%s] is not a valid Locale!", source));
        Assert.isTrue(isValidLocale(target), String.format("[Assertion failed] - Target [%s] is not a valid Locale!", target));
    }

    protected FrenglyTranslation tryRequest(String source, String target, String text, int retries) {
        URI uri = UriComponentsBuilder
                .newInstance()
                .scheme("http")
                .host(settings.getHost())
                .port(80)
                .queryParam("src", source)
                .queryParam("dest", target)
                .queryParam("text", text)
                .queryParam("email", settings.getEmail())
                .queryParam("password", settings.getPassword())
                .queryParam("outformat", "json")
                .build()
                .encode()
                .toUri();
        log.info(String.format("Obtaining translation from %s", uri.toASCIIString()));

        FrenglyTranslation result = null;
        for (int i = 0; i < retries; i++) {
            try {
                result = restTemplate.getForObject(uri, FrenglyTranslation.class);
                break;
            } catch (RestClientException e) {
                try {
                    Thread.sleep((i + 1) * 500);
                } catch (InterruptedException ie) {
                    // do nothing
                }
            }
            if (i == retries - 1) {
                result = new FrenglyTranslation(source, target, text, "Could not obtain translation!"); // FIXME i18n response
            }
        }
        return result;
    }

    @Override
    @Cacheable(cacheNames = "translations")
    public Translation obtainTranslation(String src, String target, String text) {
        counterService.increment("services.frengly.translation.invoked");
        String source = src;
        if (source == null) {
            source = settings.getDefaultSource();
        }
        validateTranslationArguments(source, target, text);
        if (src.equals(target)) {
            return new Translation(src, target, text, text);
        }
        FrenglyTranslation ft = tryRequest(source, target, text, settings.getRetries());
        return new Translation(ft.getSrc(), ft.getDest(), ft.getText(), ft.getTranslation());
    }

    @Override
    @CacheEvict("translations")
    public void evictTranslation(String source, String target, String text) {
        counterService.increment("services.frengly.cacheKey.eviction");
        log.info("Key w/ source [{}], target [{}], and text [{}] removed from cache!");
    }

}
