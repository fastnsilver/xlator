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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder(value = { "translations", "errors" })
@JsonInclude(Include.NON_EMPTY)
public class Translations {

    private List<Translation> translations;
    private List<ErrorResponse> errors;

    @JsonCreator
    public Translations(@JsonProperty("translations") List<Translation> translations,
            @JsonProperty("errors") List<ErrorResponse> errors) {
        this.translations = translations;
        this.errors = errors;
    }

    public List<Translation> getTranslations() {
        return translations;
    }

    public List<ErrorResponse> getErrors() {
        return errors;
    }

}
