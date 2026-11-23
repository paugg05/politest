package com.politest.figure.domain.model;

import com.politest.figure.domain.error.DomainValidationException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** How important each axis is to the person. Axes without an explicit weight count as 1. */
public final class AxisWeights {

    public static final double MAX = 10.0;
    private static final AxisWeights EQUAL = new AxisWeights(Map.of());

    private final Map<AxisCode, Double> weights;

    private AxisWeights(Map<AxisCode, Double> weights) {
        this.weights = weights;
    }

    /** All axes weigh the same. */
    public static AxisWeights equal() {
        return EQUAL;
    }

    /** Builds the weights from raw values by axis code. A null or empty map means equal weights. */
    public static AxisWeights of(Map<String, Double> raw) {
        if (raw == null || raw.isEmpty()) {
            return EQUAL;
        }
        Map<AxisCode, Double> weights = new EnumMap<>(AxisCode.class);
        for (Map.Entry<String, Double> entry : raw.entrySet()) {
            AxisCode axis;
            try {
                axis = AxisCode.of(entry.getKey());
            } catch (DomainValidationException e) {
                throw new DomainValidationException("Eje desconocido en «weights»: " + entry.getKey());
            }
            Double value = entry.getValue();
            if (value == null || value < 0 || value > MAX) {
                throw new DomainValidationException("El peso de " + entry.getKey() + " debe estar entre 0 y 10.");
            }
            weights.put(axis, value);
        }
        return new AxisWeights(Collections.unmodifiableMap(weights));
    }

    public double weightOf(AxisCode axis) {
        return weights.getOrDefault(axis, 1.0);
    }
}
