package com.fns.xlator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
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

    @Override
    @Cacheable(cacheNames = "translations")
    public Translation obtainTranslation(String src, String target, String text) {
        counterService.increment("services.frengly.translation.invoked");
        Assert.hasText(target, "[Assertion failed] - Target locale code must not be null, empty or blank!");
        Assert.hasText(text, "[Assertion failed] - Text to translate must not be null, empty or blank!");
        String source = src;
        if (source == null) {
            source = settings.getDefaultSource();
        }
        Assert.isTrue(isValidLocale(src), "[Assertion failed] - Source is not a valid Locale!");
        Assert.isTrue(isValidLocale(target), "[Assertion failed] - Target is not a valid Locale!");
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
        return restTemplate.getForObject(uri, Translation.class);
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
