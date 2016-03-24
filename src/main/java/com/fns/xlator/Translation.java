package com.fns.xlator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(value = { "source", "target", "text", "translation" })
public class Translation {

    private String src;
    private String dest;
    private String text;
    private String translation;

    @JsonCreator
    public Translation(@JsonProperty("src") String src, @JsonProperty("dest") String dest,
            @JsonProperty("text") String text, @JsonProperty("translation") String translation) {
        this.src = src;
        this.dest = dest;
        this.text = text;
        this.translation = translation;
    }


    public String getSource() {
        return src;
    }

    public String getTarget() {
        return dest;
    }

    public String getText() {
        return text;
    }

    public String getTranslation() {
        return translation;
    }

}
