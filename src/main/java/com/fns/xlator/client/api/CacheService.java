/*-
 * #%L
 * xlator
 * %%
 * Copyright (C) 2016 - 2018 FNS
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.fns.xlator.client.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;

@Service
public class CacheService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private MeterRegistry registry;

    @Autowired
    public CacheService(MeterRegistry registry) {
        this.registry = registry;
    }

    @CacheEvict("translations")
    public void evictTranslation(String source, String target, String text) {
        registry.counter("services.cache.key.eviction").increment();
        log.info("Key w/ source [{}], target [{}], and text [{}] removed from cache!", source, target, text);
    }

}
