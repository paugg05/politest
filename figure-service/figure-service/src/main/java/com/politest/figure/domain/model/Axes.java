package com.politest.figure.domain.model;

import java.util.List;
import java.util.Set;

/**
 * Codes of the eight ideological axes and the languages the figure texts are available in.
 *
 * <p>The axis codes are part of the contract with the main web application, which keeps its own catalogue.
 * If the axes ever change, every service must change.
 */
public final class Axes {

    public static final List<String> ORDER =
            List.of("ECON", "WELFARE", "LIBERTY", "CULTURE", "NATION", "ECOLOGY", "DEMOCRACY", "CHANGE");
    public static final Set<String> CODES = Set.copyOf(ORDER);
    /** Languages with texts in the database. */
    public static final Set<String> LANGS = Set.of("es", "ca", "en");

    private Axes() {
    }
}
