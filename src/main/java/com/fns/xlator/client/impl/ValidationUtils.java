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
