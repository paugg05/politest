package com.politest.figure.domain.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A figure together with its positions, ordered as in {@link Axes#ORDER}. */
public record FigureWithPositions(Figure figure, List<Position> positions) {

    /** Positions as a map from axis code to percentage. */
    public Map<String, Integer> asMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        positions.forEach(p -> map.put(p.axis(), p.rightPct()));
        return map;
    }
}
