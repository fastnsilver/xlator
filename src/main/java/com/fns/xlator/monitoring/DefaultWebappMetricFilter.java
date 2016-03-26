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

import java.util.HashMap;
import java.util.Map;

import com.codahale.metrics.servlet.AbstractInstrumentedFilter;

// like InstrumentedFilter except that it adds 2 more status codes: ACCEPTED (202) and CONFLICT (409)
class DefaultWebappMetricFilter extends AbstractInstrumentedFilter {
    public static final String REGISTRY_ATTRIBUTE = DefaultWebappMetricFilter.class.getName() + ".registry";

    private static final String NAME_PREFIX = "responseCodes.";
    private static final int OK = 200;
    private static final int CREATED = 201;
    private static final int ACCEPTED = 202;
    private static final int NO_CONTENT = 204;
    private static final int BAD_REQUEST = 400;
    private static final int CONFLICT = 409;
    private static final int NOT_FOUND = 404;
    private static final int SERVER_ERROR = 500;

    /**
     * Creates a new instance of the filter.
     */
    public DefaultWebappMetricFilter() {
        super(REGISTRY_ATTRIBUTE, createMeterNamesByStatusCode(), NAME_PREFIX + "other");
    }

    private static Map<Integer, String> createMeterNamesByStatusCode() {
        final Map<Integer, String> meterNamesByStatusCode = new HashMap<Integer, String>(8);
        meterNamesByStatusCode.put(OK, NAME_PREFIX + "ok");
        meterNamesByStatusCode.put(CREATED, NAME_PREFIX + "created");
        meterNamesByStatusCode.put(ACCEPTED, NAME_PREFIX + "accepted");
        meterNamesByStatusCode.put(NO_CONTENT, NAME_PREFIX + "noContent");
        meterNamesByStatusCode.put(BAD_REQUEST, NAME_PREFIX + "badRequest");
        meterNamesByStatusCode.put(NOT_FOUND, NAME_PREFIX + "notFound");
        meterNamesByStatusCode.put(CONFLICT, NAME_PREFIX + "conflict");
        meterNamesByStatusCode.put(SERVER_ERROR, NAME_PREFIX + "serverError");
        return meterNamesByStatusCode;
    }
}
