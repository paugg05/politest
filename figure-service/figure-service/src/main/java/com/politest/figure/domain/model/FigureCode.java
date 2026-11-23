package com.politest.figure.domain.model;

import com.politest.figure.domain.error.DomainValidationException;
import java.util.Locale;
import java.util.regex.Pattern;

/** Identifier of a historical figure: upper-case letters, digits and underscores (for example {@code DE_GAULLE}). */
public record FigureCode(String value) {

    private static final Pattern FORMAT = Pattern.compile("[A-Z0-9_]{1,40}");

    public FigureCode {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new DomainValidationException("Código de personaje no válido: " + value);
        }
    }

    public static FigureCode of(String value) {
        return new FigureCode(value);
    }

    /** Lenient parsing for lookups: ignores case and surrounding spaces. */
    public static FigureCode parse(String raw) {
        return new FigureCode(raw == null ? null : raw.strip().toUpperCase(Locale.ROOT));
    }
}
