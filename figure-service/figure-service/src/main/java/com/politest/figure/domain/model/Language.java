package com.politest.figure.domain.model;

import com.politest.figure.domain.error.DomainValidationException;
import java.util.Arrays;

/** Languages in which the texts about a figure are available. */
public enum Language {
    ES("es"), CA("ca"), EN("en");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    /** Two-letter code, as stored in the database and used in the API. */
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
