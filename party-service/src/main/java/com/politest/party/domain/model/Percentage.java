package com.politest.party.domain.model;

import com.politest.party.domain.error.DomainValidationException;

/**
 * A position on an axis: 0 is the left pole, 100 the right pole (same scale as the web application).
 * Immutable and always valid.
 */
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

    /** Absolute distance to another position, from 0 to 100. */
    public int distanceTo(Percentage other) {
        return Math.abs(value - other.value);
    }
}
