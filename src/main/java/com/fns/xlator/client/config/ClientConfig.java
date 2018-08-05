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
package com.fns.xlator.client.config;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ClientConfig {

	static class FrenglyConverter extends AbstractJackson2HttpMessageConverter {

		@org.springframework.lang.Nullable
		private String jsonPrefix;

		public FrenglyConverter() {
			super(Jackson2ObjectMapperBuilder.json().build(), new MediaType("text", "json", Charset.defaultCharset()));
		}

		public void setJsonPrefix(String jsonPrefix) {
			this.jsonPrefix = jsonPrefix;
		}

		public void setPrefixJson(boolean prefixJson) {
			this.jsonPrefix = (prefixJson ? ")]}', " : null);
		}

		@Override
		@SuppressWarnings("deprecation")
		protected void writePrefix(com.fasterxml.jackson.core.JsonGenerator generator, Object object) throws IOException {
			if (this.jsonPrefix != null) {
				generator.writeRaw(this.jsonPrefix);
			}
			String jsonpFunction =
					(object instanceof MappingJacksonValue ? ((MappingJacksonValue) object).getJsonpFunction() : null);
			if (jsonpFunction != null) {
				generator.writeRaw("/**/");
				generator.writeRaw(jsonpFunction + "(");
			}
		}

		@Override
		@SuppressWarnings("deprecation")
		protected void writeSuffix(com.fasterxml.jackson.core.JsonGenerator generator, Object object) throws IOException {
			String jsonpFunction =
					(object instanceof MappingJacksonValue ? ((MappingJacksonValue) object).getJsonpFunction() : null);
			if (jsonpFunction != null) {
				generator.writeRaw(");");
			}
		}
	}
	
	@Bean
    RestTemplate restTemplate(
    		ClientHttpRequestFactory clientHttpRequestFactory,
    		RestTemplateBuilder restTemplateBuilder) {
    	return restTemplateBuilder
    		.detectRequestFactory(true)
    		.build();
    }
	
	static class CustomRestTemplateCustomizer implements RestTemplateCustomizer {
	    @Override
	    public void customize(RestTemplate restTemplate) {
	        restTemplate.getMessageConverters().add(new FrenglyConverter());
	    }
	}
	
    @Bean
    CustomRestTemplateCustomizer customRestTemplateCustomizer() {
    	return new CustomRestTemplateCustomizer();
    }
    
    @Bean
    ClientHttpRequestFactory clientHttpRequestFactory() {
        int timeout = 10000;
        RequestConfig config = 
          RequestConfig.custom()
                       .setConnectTimeout(timeout)
                       .setConnectionRequestTimeout(timeout)
                       .setSocketTimeout(timeout)
                       .build();
        CloseableHttpClient client = 
                HttpClientBuilder.create().setDefaultRequestConfig(config).build();
        return new HttpComponentsClientHttpRequestFactory(client);
    }

}
