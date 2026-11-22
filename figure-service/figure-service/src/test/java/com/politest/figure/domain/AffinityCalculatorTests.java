package com.politest.figure.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

class AffinityCalculatorTests {

    private static final Map<String, Integer> FIGURE = Map.of("ECON", 80, "WELFARE", 20);

    @Test
    void identicalPositionsGive100() {
        assertEquals(100.0, AffinityCalculator.affinity(Map.of("ECON", 80, "WELFARE", 20), FIGURE, null), 1e-9);
    }

    @Test
    void oppositePositionsGive0() {
        assertEquals(0.0, AffinityCalculator.affinity(Map.of("ECON", 0, "WELFARE", 100), Map.of("ECON", 100, "WELFARE", 0), null), 1e-9);
    }

    @Test
    void usesMeanDistance() {
        // distances 10 and 30 -> mean 20 -> affinity 80
        assertEquals(80.0, AffinityCalculator.affinity(Map.of("ECON", 70, "WELFARE", 50), FIGURE, null), 1e-9);
    }

    @Test
    void weightsChangeTheResult() {
        // ECON weighs three times as much: (3*10 + 1*30) / 4 = 15 -> 85
        assertEquals(85.0, AffinityCalculator.affinity(Map.of("ECON", 70, "WELFARE", 50), FIGURE, Map.of("ECON", 3.0)), 1e-9);
    }

    @Test
    void zeroWeightIgnoresAxis() {
        assertEquals(90.0, AffinityCalculator.affinity(Map.of("ECON", 70, "WELFARE", 50), FIGURE, Map.of("WELFARE", 0.0)), 1e-9);
    }

    @Test
    void axesMissingInTheFigureAreIgnored() {
        assertEquals(100.0, AffinityCalculator.affinity(Map.of("ECON", 80, "CHANGE", 0), FIGURE, null), 1e-9);
    }

    @Test
    void noCommonAxesGives0() {
        assertEquals(0.0, AffinityCalculator.affinity(Map.of("CHANGE", 50), FIGURE, null), 1e-9);
    }
}
