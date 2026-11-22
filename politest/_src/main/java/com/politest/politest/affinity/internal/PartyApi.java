package com.politest.politest.affinity.internal;

import java.util.List;

/**
 * Forma del JSON que devuelve el party-service (el "contrato" entre los dos servicios).
 * Se copia aquí a propósito: cada microservicio tiene sus propias clases y no comparte código con el otro.
 */
public final class PartyApi {

    private PartyApi() {
    }

    public record Dataset(String version, String status, String note, String updatedOn) {
    }

    public record Gap(String axis, int user, int party, int confidence, String note, String sourceUrl) {
    }

    public record Match(String code, String name, String color, int affinity, List<Gap> axes) {
    }

    public record Response(Dataset dataset, List<Match> matches) {
    }
}
