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
package com.fns.xlator.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fns.xlator.client.api.CacheService;
import com.fns.xlator.client.api.TranslationService;
import com.fns.xlator.client.impl.TranslationException;
import com.fns.xlator.model.ErrorResponse;
import com.fns.xlator.model.Translation;
import com.fns.xlator.model.TranslationRequest;
import com.fns.xlator.model.Translations;

import io.micrometer.core.annotation.Timed;


@RestController
@RequestMapping("/translation")
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

    
    @Timed
    @GetMapping(value = "/source/{src}/target/{target}/text/{text}")
    public ResponseEntity<Translation> obtainTranslation(@PathVariable("src") String source,
            @PathVariable("target") String target, @PathVariable("text") String text) {
        return ResponseEntity.ok(translationService.obtainTranslation(source, target, text));
    }

    @Timed
    // convenience, uses a configurable default locale
    @GetMapping(value = "/target/{target}/text/{text}")
    public ResponseEntity<Translation> obtainTranslation(@PathVariable("target") String target,
            @PathVariable("text") String text) {
        return ResponseEntity.ok(translationService.obtainTranslation(defaultLocale, target, text));
    }

    @Timed
    // convenience, source x n target combinations for a text
    @GetMapping(value = "/source/{src}/targets/{targets}/text/{text}")
    public ResponseEntity<Translations> obtainTranslations(@PathVariable("src") String source,
            @PathVariable("targets") String targets, @PathVariable("text") String text, HttpServletRequest request) {
        return fetchTranslations(source, targets, text, request);
    }

    @Timed
    // convenience, default configurable locale x n target combinations for a text
    @GetMapping(value = "/targets/{targets}/text/{text}")
    public ResponseEntity<Translations> obtainTranslations(@PathVariable("targets") String targets,
            @PathVariable("text") String text, HttpServletRequest request) {
        return fetchTranslations(defaultLocale, targets, text, request);
    }

    @Timed
    // most flexible option
    @PostMapping(value = "/")
    public ResponseEntity<Translations> obtainTranslations(@RequestBody TranslationRequest[] translationRequests,
            HttpServletRequest request) {
        return fetchTranslations(translationRequests, request);
    }

    @Timed
    @DeleteMapping(value = "/source/{src}/target/{target}/text/{text}")
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
