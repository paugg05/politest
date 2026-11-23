package com.politest.party.domain.model;

/** How close a person is to a party, from 0 to 100 (100 means identical positions on every compared axis). */
public record Affinity(double value) {

    private static final double TOLERANCE = 1e-9;

    public Affinity {
        if (Double.isNaN(value) || value < -TOLERANCE || value > 100 + TOLERANCE) {
            throw new IllegalArgumentException("Affinity out of range: " + value);
        }
        value = Math.min(100.0, Math.max(0.0, value));
    }

    /** Affinity as a whole number, as shown to the user. */
    public int rounded() {
        return (int) Math.round(value);
    }
}
