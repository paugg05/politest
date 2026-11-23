package com.politest.party.domain.model;

import com.politest.party.domain.error.DomainValidationException;
import java.util.Locale;
import java.util.regex.Pattern;

/** Identifier of a party: upper-case letters, digits and underscores (for example {@code EH_BILDU}). */
public record PartyCode(String value) {

    private static final Pattern FORMAT = Pattern.compile("[A-Z0-9_]{1,40}");

    public PartyCode {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new DomainValidationException("Código de partido no válido: " + value);
        }
    }

    public static PartyCode of(String value) {
        return new PartyCode(value);
    }

    /** Lenient parsing for lookups: ignores case and surrounding spaces. */
    public static PartyCode parse(String raw) {
        return new PartyCode(raw == null ? null : raw.strip().toUpperCase(Locale.ROOT));
    }
}
