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
package com.fns.xlator.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonPropertyOrder(value = { "error", "method", "uri", "query" })
@JsonInclude(Include.NON_EMPTY)
public class ErrorResponse {
    private String method;
    private String uri;
    private String query;
    private String error;

    @JsonCreator
    public ErrorResponse(@JsonProperty String method, @JsonProperty String uri, @JsonProperty String query,
            @JsonProperty String error) {
        this.method = method;
        this.uri = uri;
        this.query = query;
        this.error = error;
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getQuery() {
        return query;
    }

    public String getError() {
        return error;
    }

    @JsonIgnore
    public String asLog() {
        return ToStringBuilder.reflectionToString(this);
    }

}

