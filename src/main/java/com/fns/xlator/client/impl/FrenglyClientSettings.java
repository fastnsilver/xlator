package com.fns.xlator.client.impl;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.frengly")
public class FrenglyClientSettings {

    private String host = "frengly.com";

    @NotEmpty
    private String email = "change@me.com";

    @NotEmpty
    private String password = "changeme";

    private int retries = 10;

    @Value("${app.defaults.locale}")
    private String defaultSource;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDefaultSource() {
        return defaultSource;
    }

    public int getRetries() {
        return retries;
    }
}
