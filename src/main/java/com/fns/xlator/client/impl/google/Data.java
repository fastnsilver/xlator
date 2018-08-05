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
package com.fns.xlator.client.impl.google;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "translations" })
public class Data {

    private List<Translation> translations;
    
    @JsonCreator
    public Data(@JsonProperty("translations") List<Translation> translations) {
        this.translations = translations;
    }

    @JsonIgnore
    public static Data error(String errorText) {
        List<Translation> translations = new ArrayList<Translation>();
        translations.add(new Translation(errorText));
        Data result = new Data(translations);
        return result;
    }

    public List<Translation> getTranslations() {
        if (translations == null) {
            translations = new ArrayList<Translation>();
        }
        return translations;
    }

}
