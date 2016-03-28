package com.fns.xlator.client.impl.google;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "translations" })
public class Data {

    private List<Translation> translations;
    
    @JsonCreator
    public Data(@JsonProperty("translations") List<Translation> translations) {
        this.translations = translations;
    }

    @JsonIgnore
    public static Data error(String errorText) {
        List<Translation> translations = new ArrayList<Translation>();
        translations.add(new Translation(errorText));
        Data result = new Data(translations);
        return result;
    }

    public List<Translation> getTranslations() {
        if (translations == null) {
            translations = new ArrayList<Translation>();
        }
        return translations;
    }

}
