package com.fns.xlator.controller;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
class Pointcuts {

    @Pointcut("execution(* com.fns.xlator.controller.TranslationController.obtain*(..)) "
            + "|| execution(* com.fns.xlator.controller.TranslationController.invalidate*(..))")
    void inTranslationController() {
    }

}