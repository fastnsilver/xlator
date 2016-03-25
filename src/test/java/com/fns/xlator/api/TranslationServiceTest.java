package com.fns.xlator.api;

import com.fns.xlator.App;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@WebAppConfiguration
public class TranslationServiceTest {

    @Autowired
    private TranslationService service;

    @Test(expected = IllegalArgumentException.class)
    public void testObtainTranslation_badLocales() {
        service.obtainTranslation("foo", "bar", "this is the text");

    }

}
