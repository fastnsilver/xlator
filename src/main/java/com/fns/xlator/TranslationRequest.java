package com.fns.xlator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(value = { "source", "target", "text" })
public class TranslationRequest {

    private String source;
    private String target;
    private String text;

    @JsonCreator
    public TranslationRequest(@JsonProperty("source") String source, @JsonProperty("target") String target,
            @JsonProperty("text") String text) {
        this.source = source;
        this.target = target;
        this.text = text;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public String getText() {
        return text;
    }

}
