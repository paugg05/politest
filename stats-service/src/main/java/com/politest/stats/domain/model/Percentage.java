package com.politest.stats.domain.model;

import com.politest.stats.domain.error.DomainValidationException;

/** A position on an axis: 0 is the left pole, 100 the right pole. Immutable and always valid. */
public record Percentage(int value) {

    public static final int MIN = 0;
    public static final int MAX = 100;

    public Percentage {
        if (value < MIN || value > MAX) {
            throw new DomainValidationException("El porcentaje debe estar entre 0 y 100: " + value);
        }
    }

    public static Percentage of(int value) {
        return new Percentage(value);
    }
}
