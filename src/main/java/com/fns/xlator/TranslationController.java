package com.fns.xlator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/translation")
public class TranslationController {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private TranslationService translationService;
    private String defaultLocale;

    @Autowired
    public TranslationController(TranslationService translationService,
            @Value("${app.defaults.locale}") String defaultLocale) {
        this.translationService = translationService;
        this.defaultLocale = defaultLocale;
    }


    @RequestMapping(value = "/source/{src}/target/{target}/text/{text}", method = RequestMethod.GET)
    public ResponseEntity<Translation> obtainTranslation(@PathVariable("src") String source,
            @PathVariable("target") String target, @PathVariable("text") String text) {
        return ResponseEntity.ok(translationService.obtainTranslation(source, target, text));
    }

    // convenience, uses a configurable default locale
    @RequestMapping(value = "/target/{target}/text/{text}", method = RequestMethod.GET)
    public ResponseEntity<Translation> obtainTranslation(@PathVariable("target") String target,
            @PathVariable("text") String text) {
        return ResponseEntity.ok(translationService.obtainTranslation(defaultLocale, target, text));
    }

    // convenience, source x n target combinations for a text
    @RequestMapping(value = "/source/{src}/targets/{targets}/text/{text}", method = RequestMethod.GET)
    public ResponseEntity<Translations> obtainTranslations(@PathVariable("src") String source,
            @PathVariable("targets") String targets, @PathVariable("text") String text, HttpServletRequest request) {
        return fetchTranslations(source, targets, text, request);
    }

    // convenience, default configurable locale x n target combinations for a text
    @RequestMapping(value = "/targets/{targets}/text/{text}", method = RequestMethod.GET)
    public ResponseEntity<Translations> obtainTranslations(@PathVariable("targets") String targets,
            @PathVariable("text") String text, HttpServletRequest request) {
        return fetchTranslations(defaultLocale, targets, text, request);
    }

    // most flexible option
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<Translations> obtainTranslations(@RequestBody TranslationRequest[] translationRequests,
            HttpServletRequest request) {
        return fetchTranslations(translationRequests, request);
    }

    protected ResponseEntity<Translations> fetchTranslations(TranslationRequest[] translationRequests,
            HttpServletRequest request) {
        List<Translation> translations = new ArrayList<>();
        List<ErrorResponse> errors = new ArrayList<>();
        Assert.isTrue(translationRequests.length <= 100,
                "[Assertion failed] - No more than 100 translations may be requested at once!");
        for (TranslationRequest tr : translationRequests) {
            try {
                translations.add(translationService.obtainTranslation(tr.getSource(), tr.getTarget(), tr.getText()));
            } catch (IllegalArgumentException iae) {
                errors.add(new ErrorResponse(request.getMethod(), request.getRequestURI(), request.getQueryString(),
                        iae.getMessage()));
            }
        }
        Translations result = new Translations(translations, errors);
        return ResponseEntity.ok(result);
    }

    protected ResponseEntity<Translations> fetchTranslations(String source, String targets, String text,
            HttpServletRequest request) {
        List<Translation> translations = new ArrayList<>();
        List<ErrorResponse> errors = new ArrayList<>();
        List<String> t = Arrays.asList(targets.split("\\s*,\\s*"));
        Assert.isTrue(t.size() <= 100, "[Assertion failed] - No more than 100 translations may be requested at once!");
        for (String target : t) {
            try {
                translations.add(translationService.obtainTranslation(source, target, text));
            } catch (IllegalArgumentException iae) {
                errors.add(new ErrorResponse(request.getMethod(), request.getRequestURI(), request.getQueryString(),
                        iae.getMessage()));
            }
        }
        Translations result = new Translations(translations, errors);
        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/source/{src}/target/{target}/text/{text}", method = RequestMethod.DELETE)
    public ResponseEntity<?> invalidateCacheKey(@PathVariable("src") String source,
            @PathVariable("target") String target, @PathVariable("text") String text) {
        translationService.evictTranslation(source, target, text);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler({ IllegalArgumentException.class })
    protected ResponseEntity<ErrorResponse> badRequest(Exception e, HttpServletRequest hsr) {
        ErrorResponse er = new ErrorResponse(hsr.getMethod(), hsr.getRequestURI(), hsr.getQueryString(),
                e.getMessage());
        log.error(er.asLog());
        return ResponseEntity.badRequest().body(er);
    }

}
