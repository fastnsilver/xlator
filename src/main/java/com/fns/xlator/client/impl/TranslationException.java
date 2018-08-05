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
package com.fns.xlator.client.impl;

public class TranslationException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    // FIXME i18n response
    
    public TranslationException(String source, String target, String text) {
        super(String.format("Could not obtain translation for source [%s], target [%s], and text [%s]", source, target, text));  
    }
    
    public TranslationException(String source, String target, String text, String reason) {
        super(String.format("Response from service was %s with source [%s], target[%s], and text [%s]", reason, source, target, text));
    }

}
