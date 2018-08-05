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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fns.xlator.Application;
import com.fns.xlator.client.api.TranslationService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes= { Application.class })
public class TranslationServiceTest {

    @Autowired
    private TranslationService service;

    @Test(expected = IllegalArgumentException.class)
    public void testObtainTranslation_badLocales() {
        service.obtainTranslation("foo", "bar", "this is the text");

    }

}
