package com.fns.xlator.client.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private CounterService counterService;

    @Autowired
    public CacheService(CounterService counterService) {
        this.counterService = counterService;
    }

    @CacheEvict("translations")
    public void evictTranslation(String source, String target, String text) {
        counterService.increment("services.frengly.cacheKey.eviction");
        log.info("Key w/ source [{}], target [{}], and text [{}] removed from cache!");
    }

}
