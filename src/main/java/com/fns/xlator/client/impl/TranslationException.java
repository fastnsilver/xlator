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
