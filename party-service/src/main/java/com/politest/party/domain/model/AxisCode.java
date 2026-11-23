package com.politest.party.domain.model;

import com.politest.party.domain.error.DomainValidationException;
import java.util.Arrays;

/**
 * One of the eight ideological axes. The declaration order is the display order.
 *
 * <p>The codes are part of the contract with the main web application, which keeps its own catalogue.
 * If the axes ever change, every service must change.
 */
public enum AxisCode {
    ECON, WELFARE, LIBERTY, CULTURE, NATION, ECOLOGY, DEMOCRACY, CHANGE;

    /** Parses an axis code, rejecting anything that is not one of the eight axes. */
    public static AxisCode of(String code) {
        return Arrays.stream(values())
                .filter(axis -> axis.name().equals(code))
                .findFirst()
                .orElseThrow(() -> new DomainValidationException("Eje desconocido: " + code));
    }
}
