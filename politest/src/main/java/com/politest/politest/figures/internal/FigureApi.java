package com.politest.politest.figures.internal;

import java.util.List;

/**
 * Forma del JSON que devuelve el figure-service (el "contrato" entre los dos servicios).
 * Se copia aquí a propósito: cada microservicio tiene sus propias clases y no comparte código con el otro.
 */
public final class FigureApi {

    private FigureApi() {
    }

    public record Dataset(String version, String status, String note, String updatedOn) {
    }

    public record Gap(String axis, int user, int figure, int confidence, String note) {
    }

    public record Match(String code, String name, String description, int birthYear, int deathYear,
                        String color, int affinity, List<Gap> axes) {
    }

    public record Response(Dataset dataset, List<Match> matches) {
    }
}
