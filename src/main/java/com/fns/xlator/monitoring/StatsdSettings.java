/*
 * Copyright 2015 - Chris Phillipson
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fns.xlator.monitoring;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "monitoring.statsd")
public class StatsdSettings {

    @Value("${spring.application.name}")
    private String applicationName;

    private String hostname;

    private Integer port = 8125;

    private boolean enabled;

    private Integer publishingIntervalInMillis = 30000;

    public String getApplicationName() {
        return applicationName;
    }

    public String getApplicationHostname() {
        String hostname = "localhost";
        try {
            hostname = InetAddress.getLocalHost().getHostName().replace('.', '_');
        } catch (UnknownHostException uhe) {
            // do nothing
        }
        final String shortName = applicationName.length() > 6 ? applicationName.substring(0, 6) : applicationName;
        return String.format("%s-%s", shortName, hostname);
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getPublishingIntervalInMillis() {
        return publishingIntervalInMillis;
    }

    public void setPublishingIntervalInMillis(Integer publishingIntervalInMillis) {
        this.publishingIntervalInMillis = publishingIntervalInMillis;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
