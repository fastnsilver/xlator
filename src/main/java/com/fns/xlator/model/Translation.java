package com.fns.xlator.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(value = { "source", "target", "text", "translation" })
public class Translation {

    private String source;
    private String target;
    private String text;
    private String translation;

    @JsonCreator
    public Translation(@JsonProperty("source") String source, @JsonProperty("target") String target,
            @JsonProperty("text") String text, @JsonProperty("translation") String translation) {
        this.source = source;
        this.target = target;
        this.text = text;
        this.translation = translation;
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

    public String getTranslation() {
        return translation;
    }

}
