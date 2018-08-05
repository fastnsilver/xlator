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
package com.fns.xlator.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.cli.CliDocumentation;
import org.springframework.restdocs.http.HttpDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import capital.scalable.restdocs.AutoDocumentation;
import capital.scalable.restdocs.jackson.JacksonResultHandlers;
import capital.scalable.restdocs.response.ResponseModifyingPreprocessors;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestDocs
public class DocumentationTest {

	@LocalServerPort
    private int port;
	
	@Autowired
	private WebApplicationContext context;

	@Autowired
	protected ObjectMapper objectMapper;

	protected MockMvc mockMvc;

	@Rule
	public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

	@Before
	public void setUp() throws Exception {
	    this.mockMvc = MockMvcBuilders
	            .webAppContextSetup(context)
	            .alwaysDo(JacksonResultHandlers.prepareJackson(objectMapper))
	            .alwaysDo(MockMvcRestDocumentation.document("{class-name}/{method-name}",
	                    Preprocessors.preprocessRequest(),
	                    Preprocessors.preprocessResponse(
	                            ResponseModifyingPreprocessors.replaceBinaryContent(),
	                            ResponseModifyingPreprocessors.limitJsonArrayLength(objectMapper),
	                            Preprocessors.prettyPrint())))
	            .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation)
	                    .uris()
	                    .withScheme("http")
	                    .withHost("localhost")
	                    .withPort(port)
	                    .and().snippets()
	                    .withDefaults(CliDocumentation.curlRequest(),
	                            HttpDocumentation.httpRequest(),
	                            HttpDocumentation.httpResponse(),
	                            AutoDocumentation.requestFields(),
	                            AutoDocumentation.responseFields(),
	                            AutoDocumentation.pathParameters(),
	                            AutoDocumentation.requestParameters(),
	                            AutoDocumentation.description(),
	                            AutoDocumentation.methodAndPath(),
	                            AutoDocumentation.section()))
	            .build();
	}
	
	@Test
    public void docsForwarding() throws Exception {
        mockMvc.perform(get("/docs"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/docs/index.html"));
    }
}
