package com.politest.stats.domain.model;

import com.politest.stats.domain.error.DomainValidationException;
import java.util.Arrays;

/** Interface languages of the web application. */
public enum Language {
    ES("es"), CA("ca"), EN("en");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static Language of(String code) {
        return Arrays.stream(values())
                .filter(language -> language.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new DomainValidationException(
                        "Idioma no soportado: " + code + " (usa es, ca o en)."));
    }
}
