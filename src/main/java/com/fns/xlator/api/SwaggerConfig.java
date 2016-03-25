package com.fns.xlator.api;

import static springfox.documentation.builders.PathSelectors.regex;
import static springfox.documentation.schema.AlternateTypeRules.newRule;

import com.fasterxml.classmate.TypeResolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.LocalDate;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Value("${app.info.version}")
    private String version;
    
    @Autowired
    private TypeResolver typeResolver;

    @Bean
    public Docket restApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .directModelSubstitute(LocalDate.class, String.class)
                .genericModelSubstitutes(ResponseEntity.class)
                .alternateTypeRules(
                        newRule(
                                typeResolver.resolve(DeferredResult.class,
                                typeResolver.resolve(ResponseEntity.class, WildcardType.class)),
                                typeResolver.resolve(WildcardType.class))
                        )
                .select()
                .paths(regex("^(/translation|/v2/api-docs).*$"))
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Xlator APIs")
                .description(
                        "This is a comprehensive list of the APIs for Xlator.\nAdditional help is provided in this project's http://fastnsilver.github.io/xlator/[Maven Site].")
                .contact(new Contact("Chris Phillipson", "http://fastnsilver.github.io/xlator/", "fastnsilver@gmail.com"))
                .version(version)
                .build();
    }
}
