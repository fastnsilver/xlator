package com.fns.xlator.client.impl.google;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "data" })
public class GoogleTranslation {

    private Data data;

    @JsonCreator
    public GoogleTranslation(@JsonProperty("data") Data data) {
        this.data = data;
    }

    public GoogleTranslation(String errorText) {
        this.data = Data.error(errorText);
    }

    public Data getData() {
        return data;
    }
}