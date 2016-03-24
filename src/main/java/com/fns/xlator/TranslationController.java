package com.fns.xlator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

    @RequestMapping(value = "/target/{target}/text/{text}", method = RequestMethod.GET)
    public ResponseEntity<Translation> obtainTranslation(@PathVariable("target") String target,
            @PathVariable("text") String text) {
        return ResponseEntity.ok(translationService.obtainTranslation(defaultLocale, target, text));
    }

    @ExceptionHandler({ IllegalArgumentException.class })
    protected ResponseEntity<ErrorResponse> badRequest(Exception e, HttpServletRequest hsr) {
        ErrorResponse er = new ErrorResponse(hsr.getMethod(), hsr.getRequestURI(), hsr.getQueryString(),
                e.getMessage());
        log.error(er.asLog());
        return ResponseEntity.badRequest().body(er);
    }

    @JsonPropertyOrder(value = { "error", "method", "uri", "query" })
    @JsonInclude(Include.NON_EMPTY)
    class ErrorResponse {
        private String method;
        private String uri;
        private String query;
        private String error;

        @JsonCreator
        public ErrorResponse(@JsonProperty String method, @JsonProperty String uri, @JsonProperty String query,
                @JsonProperty String error) {
            this.method = method;
            this.uri = uri;
            this.query = query;
            this.error = error;
        }

        public String getMethod() {
            return method;
        }

        public String getUri() {
            return uri;
        }

        public String getQuery() {
            return query;
        }

        public String getError() {
            return error;
        }

        @JsonIgnore
        public String asLog() {
            return ToStringBuilder.reflectionToString(this);
        }

    }

}
