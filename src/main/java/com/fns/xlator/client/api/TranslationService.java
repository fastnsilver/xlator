package com.fns.xlator.client.api;

import com.fns.xlator.model.Translation;

public interface TranslationService {

    public Translation obtainTranslation(String source, String target, String text);

    public void evictTranslation(String source, String target, String text);

}
