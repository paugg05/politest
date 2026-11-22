package com.politest.politest;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;

@Configuration
class WebConfig {

    static final List<String> SUPPORTED_LANGS = List.of("ca", "es", "en");
    static final String DEFAULT_LANG = "es";

    @Bean
    LocaleResolver localeResolver() {
        return new PathLocaleResolver();
    }
}
