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

import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;

public class ValidationUtils {

    // FIXME i18n responses

    private static Locale parseLocale(String locale) {
        String[] parts = locale.split("_");
        switch (parts.length) {
            case 3:
                return new Locale(parts[0], parts[1], parts[2]);
            case 2:
                return new Locale(parts[0], parts[1]);
            case 1:
                return new Locale(parts[0]);
            default:
                throw new IllegalArgumentException("Invalid locale: " + locale);
        }
    }

    private static boolean isValidLocale(String code) {
        try {
            Locale locale = parseLocale(code);
            return Arrays.asList(Locale.getAvailableLocales()).contains(locale);
        } catch (MissingResourceException e) {
            return false;
        }
    }

    public static void validateTranslationArguments(String source, String target, String text) {
        Assert.hasText(target, "[Assertion failed] - Target locale code must not be null, empty or blank!");
        Assert.hasText(text, "[Assertion failed] - Text to translate must not be null, empty or blank!");
        Assert.isTrue(isValidLocale(source), String.format("[Assertion failed] - Source [%s] is not a valid Locale!", source));
        Assert.isTrue(isValidLocale(target), String.format("[Assertion failed] - Target [%s] is not a valid Locale!", target));
    }

}
