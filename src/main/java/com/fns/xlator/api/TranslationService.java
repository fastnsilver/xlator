package com.fns.xlator.api;

import com.fns.xlator.model.Translation;

public interface TranslationService {

    Translation obtainTranslation(String source, String target, String text);
    void evictTranslation(String source, String target, String text);

}
