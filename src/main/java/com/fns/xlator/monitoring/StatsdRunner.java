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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.readytalk.metrics.StatsDReporter;

public class StatsdRunner implements CommandLineRunner {

    @Autowired
    private MetricRegistry metricRegistry;

    @Autowired
    private StatsdSettings statsdSettings;

    @Autowired
    private StatsDReporter reporter;

    @Override
    public void run(String... strings) throws Exception {
        // JVM metrics ala Dropwizard metrics-jvm
        metricRegistry.registerAll(new MemoryUsageGaugeSet());
        metricRegistry.registerAll(new ThreadStatesGaugeSet());
        metricRegistry.registerAll(new GarbageCollectorMetricSet());
        metricRegistry.registerAll(new ClassLoadingGaugeSet());
        // start collecting w/ statsd via ReadyTalk client
        reporter.start(statsdSettings.getPublishingIntervalInMillis(), TimeUnit.MILLISECONDS);
    }
}
