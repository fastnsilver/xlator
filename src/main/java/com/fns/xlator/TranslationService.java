package com.fns.xlator;

public interface TranslationService {

    Translation obtainTranslation(String source, String target, String text);
    void evictTranslation(String source, String target, String text);

}
