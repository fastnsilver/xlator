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
package com.fns.xlator.client.impl.frengly;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(content = Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(value = { "src", "dest", "text", "email", "password" })
public class FrenglyRequest {

    private String src;
    private String dest;
    private String text;
    private String email;
    private String password;

    @JsonCreator
    public FrenglyRequest(
    		@JsonProperty("src") String src, 
    		@JsonProperty("dest") String dest,
            @JsonProperty("text") String text, 
            @JsonProperty("email") String email,
            @JsonProperty("password") String password) {
        this.src = src;
        this.dest = dest;
        this.text = text;
        this.email = email;
        this.password = password;
    }


    public String getSrc() {
        return src;
    }

    public String getDest() {
        return dest;
    }

    public String getText() {
        return text;
    }

    public String getEmail() {
        return email;
    }
    
    public String getPassword() {
    	return password;
    }

}
