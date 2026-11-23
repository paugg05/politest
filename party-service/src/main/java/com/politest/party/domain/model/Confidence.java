package com.politest.party.domain.model;

import com.politest.party.domain.error.DomainValidationException;
import java.util.Arrays;

/** How well founded an estimated position is. */
public enum Confidence {
    WEAK(1), PARTIAL(2), SOLID(3);

    private final int level;

    Confidence(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }

    public static Confidence ofLevel(int level) {
        return Arrays.stream(values())
                .filter(confidence -> confidence.level == level)
                .findFirst()
                .orElseThrow(() -> new DomainValidationException("Nivel de confianza no válido: " + level));
    }
}
