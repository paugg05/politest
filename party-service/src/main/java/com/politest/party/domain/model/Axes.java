package com.politest.party.domain.model;

import java.util.List;
import java.util.Set;

/**
 * Codes of the eight ideological axes.
 *
 * <p>These codes are part of the contract with the main web application, which keeps its own catalogue.
 * If the axes ever change, both sides must change.
 */
public final class Axes {

    public static final List<String> ORDER =
            List.of("ECON", "WELFARE", "LIBERTY", "CULTURE", "NATION", "ECOLOGY", "DEMOCRACY", "CHANGE");
    public static final Set<String> CODES = Set.copyOf(ORDER);

    private Axes() {
    }
}
