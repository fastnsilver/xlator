package com.fns.xlator.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder(value = { "translations", "errors" })
@JsonInclude(Include.NON_EMPTY)
public class Translations {

    private List<Translation> translations;
    private List<ErrorResponse> errors;

    @JsonCreator
    public Translations(@JsonProperty("translations") List<Translation> translations,
            @JsonProperty("errors") List<ErrorResponse> errors) {
        this.translations = translations;
        this.errors = errors;
    }

    public List<Translation> getTranslations() {
        return translations;
    }

    public List<ErrorResponse> getErrors() {
        return errors;
    }

}
