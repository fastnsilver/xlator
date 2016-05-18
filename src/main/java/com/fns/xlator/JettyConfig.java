package com.fns.xlator;

import static com.codahale.metrics.MetricRegistry.name;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.RatioGauge;

import org.eclipse.jetty.server.LowResourceMonitor;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties
class JettyConfig {

    @Configuration
    @ConfigurationProperties(prefix = "jetty.threadPool")
    static class JettyThreadPoolSettings {

        private Integer minThreads = 8;
        private Integer maxThreads = 32;
        private Integer idleTimeout = 30000;
        private boolean detailedDump;

        public Integer getMinThreads() {
            return minThreads;
        }

        public void setMinThreads(Integer minThreads) {
            this.minThreads = minThreads;
        }

        public Integer getMaxThreads() {
            return maxThreads;
        }

        public void setMaxThreads(Integer maxThreads) {
            this.maxThreads = maxThreads;
        }

        public Integer getIdleTimeout() {
            return idleTimeout;
        }

        public void setIdleTimeout(Integer idleTimeout) {
            this.idleTimeout = idleTimeout;
        }

        public boolean isDetailedDump() {
            return detailedDump;
        }

        public void setDetailedDump(boolean detailedDump) {
            this.detailedDump = detailedDump;
        }

    }

    @Configuration
    @ConfigurationProperties(prefix = "jetty.lowResources")
    // @see http://eclipse.org/jetty/documentation/current/limit-load.html
    // @see http://eclipse.org/jetty/documentation/current/embedding-jetty.html#d0e19129
    static class JettyLowResourceMonitorSettings {
        // all durations in milliseconds

        private Integer period = 1000;
        private Integer idleTimeout = 200;
        private boolean monitorThreads = true;
        private Integer maxConnections = 0;
        private Integer maxMemory = 0;
        private Integer maxLowResourcesTime = 5000;

        public Integer getPeriod() {
            return period;
        }

        public void setPeriod(Integer period) {
            this.period = period;
        }

        public Integer getIdleTimeout() {
            return idleTimeout;
        }

        public void setIdleTimeout(Integer idleTimeout) {
            this.idleTimeout = idleTimeout;
        }

        public boolean isMonitorThreads() {
            return monitorThreads;
        }

        public void setMonitorThreads(boolean monitorThreads) {
            this.monitorThreads = monitorThreads;
        }

        public Integer getMaxConnections() {
            return maxConnections;
        }

        public void setMaxConnections(Integer maxConnections) {
            this.maxConnections = maxConnections;
        }

        public Integer getMaxMemory() {
            return maxMemory;
        }

        public void setMaxMemory(Integer maxMemory) {
            this.maxMemory = maxMemory;
        }

        public Integer getMaxLowResourcesTime() {
            return maxLowResourcesTime;
        }

        public void setMaxLowResourcesTime(Integer maxLowResourcesTime) {
            this.maxLowResourcesTime = maxLowResourcesTime;
        }

    }

    // @see http://wiki.eclipse.org/Jetty/Howto/High_Load
    // @see http://jdpgrailsdev.github.io/blog/2014/10/07/spring_boot_jetty_thread_pool.html
    @Primary
    @Bean
    public JettyEmbeddedServletContainerFactory jettyEmbeddedServletContainerFactory(MetricRegistry metricRegistry,
            JettyThreadPoolSettings jettyThreadPoolSettings,
            JettyLowResourceMonitorSettings jettyLowResourceMonitorSettings,
            @Value("${server.port:8080}") final int port) {
        final JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory(port);
        factory.addServerCustomizers(
                new InstrumentedJettyServer(metricRegistry, jettyThreadPoolSettings, jettyLowResourceMonitorSettings));
        return factory;
    }

    private class InstrumentedJettyServer implements JettyServerCustomizer {

        private final MetricRegistry metricRegistry;
        private final JettyThreadPoolSettings jettyThreadPoolSettings;
        private final JettyLowResourceMonitorSettings jettyLowResourceMonitorSettings;

        public InstrumentedJettyServer(MetricRegistry metricRegistry, JettyThreadPoolSettings jettyThreadPoolSettings,
                JettyLowResourceMonitorSettings jettyLowResourceMonitorSettings) {
            this.metricRegistry = metricRegistry;
            this.jettyThreadPoolSettings = jettyThreadPoolSettings;
            this.jettyLowResourceMonitorSettings = jettyLowResourceMonitorSettings;
        }

        @Override
        public void customize(Server server) {
            QueuedThreadPool pool = (QueuedThreadPool) server.getThreadPool();
            pool.setName("jetty-pool");

            configureThreadPool(pool);
            configureLowResourceMonitor(server);
            configureMetrics(pool);
        }

        protected void configureThreadPool(QueuedThreadPool pool) {
            // override QueuedThreadPool defaults
            pool.setMaxThreads(jettyThreadPoolSettings.getMaxThreads());
            pool.setMinThreads(jettyThreadPoolSettings.getMinThreads());
            pool.setIdleTimeout(jettyThreadPoolSettings.getIdleTimeout());
            pool.setDetailedDump(jettyThreadPoolSettings.isDetailedDump());
        }

        protected void configureLowResourceMonitor(Server server) {
            // monitor low resources
            LowResourceMonitor lowResourcesMonitor = new LowResourceMonitor(server);
            lowResourcesMonitor.setPeriod(jettyLowResourceMonitorSettings.getPeriod());
            lowResourcesMonitor.setLowResourcesIdleTimeout(jettyLowResourceMonitorSettings.getIdleTimeout());
            lowResourcesMonitor.setMonitorThreads(jettyLowResourceMonitorSettings.isMonitorThreads());
            lowResourcesMonitor.setMaxConnections(jettyLowResourceMonitorSettings.getMaxConnections());
            lowResourcesMonitor.setMaxMemory(jettyLowResourceMonitorSettings.getMaxMemory());
            lowResourcesMonitor.setMaxLowResourcesTime(jettyLowResourceMonitorSettings.getMaxLowResourcesTime());
            server.addBean(lowResourcesMonitor);
        }

        protected void configureMetrics(QueuedThreadPool pool) {
            // metrics
            metricRegistry.register(name(pool.getName(), "utilization"), new RatioGauge() {
                @Override
                protected Ratio getRatio() {
                    return Ratio.of(pool.getThreads() - pool.getIdleThreads(), pool.getThreads());
                }
            });
            metricRegistry.register(name(pool.getName(), "utilization-max"), new RatioGauge() {
                @Override
                protected Ratio getRatio() {
                    return Ratio.of(pool.getThreads() - pool.getIdleThreads(), pool.getMaxThreads());
                }
            });
            metricRegistry.register(name(pool.getName(), "size"), new Gauge<Integer>() {
                @Override
                public Integer getValue() {
                    return pool.getThreads();
                }
            });
            metricRegistry.register(name(pool.getName(), "jobs"), new Gauge<Integer>() {
                @Override
                public Integer getValue() {
                    // This assumes the QueuedThreadPool is using a BlockingArrayQueue or
                    // ArrayBlockingQueue for its queue, and is therefore a constant-time operation.
                    return pool.getQueueSize();
                }
            });
        }

    }

}
