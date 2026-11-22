package com.politest.figure.figure;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FigureModels {

    private FigureModels() {
    }

    public record Figure(String code, String name, String description, int birthYear, int deathYear, String color) {
    }

    /**
     * Posición de un personaje en un eje.
     *
     * @param rightPct   0 = polo izquierdo del eje, 100 = polo derecho (misma escala que la web principal)
     * @param confidence 1 = estimación con poca base, 2 = base parcial, 3 = base sólida
     * @param note       justificación, en español
     */
    public record Position(String axis, int rightPct, int confidence, String note) {
    }

    public record FigureWithPositions(Figure figure, List<Position> positions) {

        public Map<String, Integer> asMap() {
            Map<String, Integer> map = new LinkedHashMap<>();
            positions.forEach(p -> map.put(p.axis(), p.rightPct()));
            return map;
        }
    }

    public record DatasetInfo(String version, String status, String note, LocalDate updatedOn) {
    }
}
