package com.fns.xlator.controller;

import com.fns.xlator.client.api.CacheService;
import com.fns.xlator.client.api.TranslationService;
import com.fns.xlator.client.impl.TranslationException;
import com.fns.xlator.model.ErrorResponse;
import com.fns.xlator.model.Translation;
import com.fns.xlator.model.TranslationRequest;
import com.fns.xlator.model.Translations;

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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/translation")
@Api(value = "translation", produces = "application/json")
public class TranslationController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private CacheService cacheService;
    private TranslationService translationService;
    private String defaultLocale;
    private int maximumTranslations;

    @Autowired
    public TranslationController(CacheService cacheService, TranslationService translationService,
            @Value("${app.defaults.locale}") String defaultLocale,
            @Value("${app.limits.translationsPerRequest}") int maximumTranslations) {
        this.cacheService = cacheService;
        this.translationService = translationService;
        this.defaultLocale = defaultLocale;
        this.maximumTranslations = maximumTranslations;
    }

    
    protected ResponseEntity<Translations> fetchTranslations(TranslationRequest[] translationRequests,
            HttpServletRequest request) {
        List<Translation> translations = new ArrayList<>();
        List<ErrorResponse> errors = new ArrayList<>();
        Assert.isTrue(translationRequests.length <= maximumTranslations, String.format(
                "[Assertion failed] - No more than %d translations may be requested at once!", maximumTranslations));
        for (TranslationRequest tr : translationRequests) {
            try {
                translations.add(translationService.obtainTranslation(tr.getSource(), tr.getTarget(), tr.getText()));
            } catch (IllegalArgumentException | TranslationException e) {
                errors.add(new ErrorResponse(request.getMethod(), request.getRequestURI(), request.getQueryString(),
                        e.getMessage()));
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
        Assert.isTrue(t.size() <= maximumTranslations, String.format(
                "[Assertion failed] - No more than %d translations may be requested at once!", maximumTranslations));
        for (String target : t) {
            try {
                translations.add(translationService.obtainTranslation(source, target, text));
            } catch (IllegalArgumentException | TranslationException e) {
                errors.add(new ErrorResponse(request.getMethod(), request.getRequestURI(), request.getQueryString(),
                        e.getMessage()));
            }
        }
        Translations result = new Translations(translations, errors);
        return ResponseEntity.ok(result);
    }


    @RequestMapping(value = "/source/{src}/target/{target}/text/{text}", method = RequestMethod.GET)
    @ApiOperation(httpMethod = "GET", notes = "Obtain a translation.", value = "/translation/source/{src}/target/{target}/text/{text}")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Successfully obtained translation."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.") 
    })
    public ResponseEntity<Translation> obtainTranslation(@PathVariable("src") String source,
            @PathVariable("target") String target, @PathVariable("text") String text) {
        return ResponseEntity.ok(translationService.obtainTranslation(source, target, text));
    }

    // convenience, uses a configurable default locale
    @RequestMapping(value = "/target/{target}/text/{text}", method = RequestMethod.GET)
    @ApiOperation(httpMethod = "GET", notes = "Obtain a translation using default locale.", value = "/translation/target/{target}/text/{text}")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Successfully obtained translation."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.") 
    })
    public ResponseEntity<Translation> obtainTranslation(@PathVariable("target") String target,
            @PathVariable("text") String text) {
        return ResponseEntity.ok(translationService.obtainTranslation(defaultLocale, target, text));
    }

    // convenience, source x n target combinations for a text
    @RequestMapping(value = "/source/{src}/targets/{targets}/text/{text}", method = RequestMethod.GET)
    @ApiOperation(httpMethod = "GET", notes = "Obtain translations for each target locale.", value = "/translation/source/{src}/targets/{targets}/text/{text}")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Obtained translations.  (Report errors with any target)."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.") 
    })
    public ResponseEntity<Translations> obtainTranslations(@PathVariable("src") String source,
            @PathVariable("targets") String targets, @PathVariable("text") String text, HttpServletRequest request) {
        return fetchTranslations(source, targets, text, request);
    }

    // convenience, default configurable locale x n target combinations for a text
    @RequestMapping(value = "/targets/{targets}/text/{text}", method = RequestMethod.GET)
    @ApiOperation(httpMethod = "GET", notes = "Obtain translations for each target locale using default locale.", value = "/translation/targets/{targets}/text/{text}")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Obtained translations.  (Report errors with any target)."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.") 
    })
    public ResponseEntity<Translations> obtainTranslations(@PathVariable("targets") String targets,
            @PathVariable("text") String text, HttpServletRequest request) {
        return fetchTranslations(defaultLocale, targets, text, request);
    }

    // most flexible option
    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ApiOperation(httpMethod = "POST", notes = "Obtain translations.", value = "/translation/")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Obtained translations.  (Report errors with any source-target-text combination)."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.") 
    })
    public ResponseEntity<Translations> obtainTranslations(@RequestBody TranslationRequest[] translationRequests,
            HttpServletRequest request) {
        return fetchTranslations(translationRequests, request);
    }

    @RequestMapping(value = "/source/{src}/target/{target}/text/{text}", method = RequestMethod.DELETE)
    @ApiOperation(httpMethod = "DELETE", notes = "Evict cache-key for a previously obtained translation.", value = "/translation/source/{src}/target/{target}/text/{text}")
    @ApiResponses(value = { 
            @ApiResponse(code = 204, message = "Successfully evicted key."),
            @ApiResponse(code = 500, message = "Internal server error.") 
    })
    public ResponseEntity<?> invalidateCacheKey(@PathVariable("src") String source,
            @PathVariable("target") String target, @PathVariable("text") String text) {
        cacheService.evictTranslation(source, target, text);
        log.info(String.format("Evicted cache key for /source/%s/target/%s/text/%s", source, target, text));
        return ResponseEntity.noContent().build();
    }

    
    @ExceptionHandler({ IllegalArgumentException.class, TranslationException.class })
    protected ResponseEntity<ErrorResponse> badRequest(Exception e, HttpServletRequest hsr) {
        ErrorResponse er = new ErrorResponse(hsr.getMethod(), hsr.getRequestURI(), hsr.getQueryString(),
                e.getMessage());
        log.error(er.asLog());
        return ResponseEntity.badRequest().body(er);
    }

}
