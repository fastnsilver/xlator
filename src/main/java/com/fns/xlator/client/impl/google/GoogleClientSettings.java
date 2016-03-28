package com.fns.xlator.client.impl.google;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.google")
public class GoogleClientSettings {

    private String host = "www.googleapis.com";

    @NotEmpty
    private String key = "changeme";

    private int retries = 10;

    @Value("${app.defaults.locale:en}")
    private String defaultSource;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDefaultSource() {
        return defaultSource;
    }

    public int getRetries() {
        return retries;
    }
}
