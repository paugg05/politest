package com.politest.party.domain.model;

import com.politest.party.domain.error.DomainValidationException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** The positions of a person on the axes they answered. Never empty, always within range. */
public final class AxisScores {

    private final Map<AxisCode, Percentage> scores;

    private AxisScores(Map<AxisCode, Percentage> scores) {
        this.scores = scores;
    }

    /** Builds the scores from raw values by axis code, validating every axis and value. */
    public static AxisScores of(Map<String, Integer> raw) {
        if (raw == null || raw.isEmpty()) {
            throw new DomainValidationException("Falta «scores»: porcentajes por eje.");
        }
        Map<AxisCode, Percentage> scores = new EnumMap<>(AxisCode.class);
        for (Map.Entry<String, Integer> entry : raw.entrySet()) {
            AxisCode axis = AxisCode.of(entry.getKey());
            Integer value = entry.getValue();
            if (value == null || value < Percentage.MIN || value > Percentage.MAX) {
                throw new DomainValidationException(
                        "El porcentaje de " + entry.getKey() + " debe estar entre 0 y 100.");
            }
            scores.put(axis, Percentage.of(value));
        }
        return new AxisScores(Collections.unmodifiableMap(scores));
    }

    public Optional<Percentage> on(AxisCode axis) {
        return Optional.ofNullable(scores.get(axis));
    }

    public Set<AxisCode> axes() {
        return scores.keySet();
    }
}
