package com.fns.xlator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(value = { "src", "dest", "text", "translation" })
public class FrenglyTranslation {

    private String src;
    private String dest;
    private String text;
    private String translation;

    @JsonCreator
    public FrenglyTranslation(@JsonProperty("src") String src, @JsonProperty("dest") String dest,
            @JsonProperty("text") String text, @JsonProperty("translation") String translation) {
        this.src = src;
        this.dest = dest;
        this.text = text;
        this.translation = translation;
    }


    public String getSrc() {
        return src;
    }

    public String getDest() {
        return dest;
    }

    public String getText() {
        return text;
    }

    public String getTranslation() {
        return translation;
    }

}
