package com.fns.xlator.client.impl.google;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "translatedText" })
public class Translation {

    private String translatedText;

    @JsonCreator
    public Translation(@JsonProperty("translatedText") String translatedText) {
        this.translatedText = translatedText;
    }

    public String getTranslatedText() {
        return translatedText;
    }
}