package com.fns.xlator.cache;

import org.springframework.boot.actuate.cache.CacheStatistics;
import org.springframework.boot.actuate.cache.CacheStatisticsProvider;
import org.springframework.boot.actuate.cache.DefaultCacheStatistics;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;

import java.util.Properties;

// Naive stats collection implementation, only capable of working with one cluster node
// to be replaced when https://github.com/spring-projects/spring-boot/issues/4966 is addressed
public class RedisCacheStatisticsProvider implements CacheStatisticsProvider<RedisCache> {

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisCacheStatisticsProvider(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public CacheStatistics getCacheStatistics(CacheManager cacheManager, RedisCache cache) {
        DefaultCacheStatistics statistics = new DefaultCacheStatistics();
        // @see http://redis.io/commands/INFO
        RedisConnection connection = RedisConnectionUtils.getConnection(this.redisConnectionFactory);
        try {
            Properties props = connection.info();
            Long hitCount = Long.parseLong(props.getProperty("keyspace_hits"));
            Long missCount = Long.parseLong(props.getProperty("keyspace_misses"));
            statistics.setGetCacheCounts(hitCount, missCount);
        } finally {
            RedisConnectionUtils.releaseConnection(connection, this.redisConnectionFactory);
        }
        return statistics;
    }

}
