package com.fns.xlator.client.impl.google;

import com.fns.xlator.Application;
import com.fns.xlator.client.api.TranslationService;
import com.fns.xlator.client.impl.TranslationException;
import com.fns.xlator.client.impl.ValidationUtils;
import com.fns.xlator.model.Translation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class GoogleTranslationService implements TranslationService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private RestTemplate restTemplate;
    private GoogleClientSettings settings;
    private CounterService counterService;

    @Autowired
    public GoogleTranslationService(RestTemplate restTemplate, GoogleClientSettings settings,
            CounterService counterService) {
        this.restTemplate = restTemplate;
        this.settings = settings;
        this.counterService = counterService;
    }

    // @see https://cloud.google.com/translate/v2/using_rest#Translate
    protected GoogleTranslation tryRequest(String source, String target, String text, int retries) {
        URI uri = UriComponentsBuilder
                .newInstance()
                .scheme("https")
                .host(settings.getHost())
                .path("language/translate/v2")
                .queryParam("key", settings.getKey())
                .queryParam("source", source)
                .queryParam("target", target)
                .queryParam("q", text)
                .build()
                .encode().toUri();
        log.info(String.format("Obtaining translation from %s", uri.toASCIIString()));

        GoogleTranslation result = null;
        for (int i = 0; i < retries; i++) {
            try {
                result = restTemplate.getForObject(uri, GoogleTranslation.class);
                break;
            } catch (final HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                    throw new TranslationException(source, target, text, e.getMessage());
                }
                try {
                    // 0.5 ms multiplier delay for subsequent retries
                    TimeUnit.MICROSECONDS.sleep((i + 1) * 500);
                } catch (final InterruptedException ie) {
                    // do nothing
                }
            }
        }
        return result;
    }

    @Override
    @Cacheable(cacheNames = Application.CACHE_NAME)
    public Translation obtainTranslation(String src, String target, String text) {
        counterService.increment("services.google.translation.invoked");
        String source = src;
        if (source == null) {
            source = settings.getDefaultSource();
        }
        ValidationUtils.validateTranslationArguments(source, target, text);
        if (src.equals(target)) {
            return new Translation(src, target, text, text);
        }
        GoogleTranslation gt = tryRequest(source, target, text, settings.getRetries());
        return new Translation(source, target, text, gt.getData().getTranslations().get(0).getTranslatedText());
    }

}
