package com.politest.party.party;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class PartyModels {

    private PartyModels() {
    }

    public record Party(String code, String name, String color) {
    }

    /**
     * Posición de un partido en un eje.
     *
     * @param rightPct   0 = polo izquierdo del eje, 100 = polo derecho (misma escala que la web principal)
     * @param confidence 1 = estimación con poca base, 2 = base parcial, 3 = base sólida
     * @param note       justificación, en español
     * @param sourceUrl  fuente consultada (puede ser nula si es una estimación por trayectoria)
     */
    public record Position(String axis, int rightPct, int confidence, String note, String sourceUrl) {
    }

    /** Un partido con sus posiciones (ordenadas como Axes.ORDER). */
    public record PartyWithPositions(Party party, List<Position> positions) {

        public Map<String, Integer> asMap() {
            Map<String, Integer> map = new java.util.LinkedHashMap<>();
            positions.forEach(p -> map.put(p.axis(), p.rightPct()));
            return map;
        }
    }

    public record DatasetInfo(String version, String status, String note, LocalDate updatedOn) {
    }
}
