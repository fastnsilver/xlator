package com.fns.xlator.client.impl.frengly;

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

    // @see http://www.frengly.com/#!/api
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
                .encode().toUri();
        log.info(String.format("Obtaining translation from %s", uri.toASCIIString()));

        FrenglyTranslation result = null;
        for (int i = 0; i < retries; i++) {
            try {
                result = restTemplate.getForObject(uri, FrenglyTranslation.class);
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
        counterService.increment("services.frengly.translation.invoked");
        String source = src;
        if (source == null) {
            source = settings.getDefaultSource();
        }
        ValidationUtils.validateTranslationArguments(source, target, text);
        if (src.equals(target)) {
            return new Translation(src, target, text, text);
        }
        FrenglyTranslation ft = tryRequest(source, target, text, settings.getRetries());
        return new Translation(ft.getSrc(), ft.getDest(), ft.getText(), ft.getTranslation());
    }

}
