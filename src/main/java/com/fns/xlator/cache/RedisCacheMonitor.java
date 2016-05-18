package com.fns.xlator.cache;

import org.springframework.boot.actuate.cache.DefaultCacheStatistics;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.dropwizard.DropwizardMetricServices;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class RedisCacheMonitor {

    private final RedisConnectionFactory redisConnectionFactory;
    private final DropwizardMetricServices metricServices;

    public RedisCacheMonitor(RedisConnectionFactory redisConnectionFactory, DropwizardMetricServices metricServices) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.metricServices = metricServices;
    }

    @Scheduled(fixedDelay = 5000)
    public void monitor() {
        DefaultCacheStatistics statistics = new DefaultCacheStatistics();
        // @see http://redis.io/commands/INFO
        RedisConnection connection = RedisConnectionUtils.getConnection(this.redisConnectionFactory);
        try {
            Properties props = connection.info();
            Long hitCount = Long.parseLong(props.getProperty("keyspace_hits"));
            Long missCount = Long.parseLong(props.getProperty("keyspace_misses"));
            statistics.setGetCacheCounts(hitCount, missCount);
            // we do not currently have a way of calculating the cache size, so we have to filter
            List<Metric<?>> metrics = statistics
                                        .toMetrics("cache.")
                                            .stream()
                                                .filter(f -> !f.getName().contains(".size"))
                                                .collect(Collectors.toList());
            metrics.forEach(m -> metricServices.submit(m.getName(), (Double) m.getValue()));
        } finally {
            RedisConnectionUtils.releaseConnection(connection, this.redisConnectionFactory);
        }
    }
}
