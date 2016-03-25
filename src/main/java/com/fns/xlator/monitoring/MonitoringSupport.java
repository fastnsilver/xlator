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

import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.MetricRepositoryAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlet.InstrumentedFilterContextListener;
import com.readytalk.metrics.StatsDReporter;

@Configuration
@EnableScheduling
@AutoConfigureAfter(MetricRepositoryAutoConfiguration.class)
@EnableConfigurationProperties(StatsdSettings.class)
@ConditionalOnProperty(prefix = "monitoring.statsd", name = "enabled", havingValue = "true")
public class MonitoringSupport {

    @Autowired
    private StatsdSettings statsdSettings;

    // @see http://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-metrics.html#production-ready-code-hale-metrics
    @Autowired
    private MetricRegistry metricRegistry;

    // @see https://github.com/ReadyTalk/metrics-statsd
    @Bean
    public StatsDReporter statsDReporter() {
        // @formatter:off
        return StatsDReporter
                .forRegistry(metricRegistry)
                .prefixedWith(String.format("%s.%s", statsdSettings.getApplicationName(), statsdSettings.getApplicationHostname()))
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .convertRatesTo(TimeUnit.SECONDS)
                .filter(MetricFilter.ALL)
                .build(statsdSettings.getHostname(), statsdSettings.getPort());
        // @formatter:on
    }

    @Bean
    public StatsdRunner statsdRunner() {
        return new StatsdRunner();
    }

    // @see https://dropwizard.github.io/metrics/3.1.0/manual/servlet/
    @Bean(name = "filter:io.dropwizard.webAppMetrics")
    @DependsOn(value = "listener:io.dropwizard.monitoring")
    public FilterRegistrationBean webAppMetricsFilter() {
        final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new DefaultWebappMetricFilter());
        return registrationBean;
    }
    
    @Bean(name = "listener:io.dropwizard.monitoring")
    public MonitoringListener monitoringListener() {
        return new MonitoringListener(metricRegistry);
    }

    protected final class MonitoringListener extends InstrumentedFilterContextListener {

        private MetricRegistry metricRegistry;

        public MonitoringListener(MetricRegistry metricRegistry) {
            this.metricRegistry = metricRegistry;
        }

        @Override
        protected MetricRegistry getMetricRegistry() {
            return metricRegistry;
        }

        @Override
        public void contextInitialized(ServletContextEvent sce) {
            sce.getServletContext().setAttribute(DefaultWebappMetricFilter.REGISTRY_ATTRIBUTE, getMetricRegistry());
        }
    }

}