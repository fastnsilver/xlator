package com.fns.xlator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
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

    protected void validateTranslationArguments(String source, String target, String text) {
        Assert.hasText(target, "[Assertion failed] - Target locale code must not be null, empty or blank!");
        Assert.hasText(text, "[Assertion failed] - Text to translate must not be null, empty or blank!");
        Assert.isTrue(isValidLocale(source), "[Assertion failed] - Source is not a valid Locale!");
        Assert.isTrue(isValidLocale(target), "[Assertion failed] - Target is not a valid Locale!");
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
        FrenglyTranslation ft = restTemplate.getForObject(uri, FrenglyTranslation.class);
        return new Translation(ft.getSrc(), ft.getDest(), ft.getText(), ft.getTranslation());
    }

    @Override
    @CacheEvict("translations")
    public void evictTranslation(String source, String target, String text) {
        counterService.increment("services.frengly.cacheKey.eviction");
        log.info("Key w/ source [{}], target [{}], and text [{}] removed from cache!");
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
            return locale.getISO3Language() != null && locale.getISO3Country() != null;
        } catch (MissingResourceException e) {
            return false;
        }
    }

}
