package com.politest.party.affinity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.Test;

class AffinityCalculatorTests {

    private static final Map<String, Integer> PARTY = Map.of("ECON", 80, "WELFARE", 20);

    @Test
    void identicalPositionsGive100() {
        assertEquals(100.0, AffinityCalculator.affinity(Map.of("ECON", 80, "WELFARE", 20), PARTY, null), 1e-9);
    }

    @Test
    void oppositePositionsGive0() {
        assertEquals(0.0, AffinityCalculator.affinity(Map.of("ECON", 0, "WELFARE", 100), Map.of("ECON", 100, "WELFARE", 0), null), 1e-9);
    }

    @Test
    void usesMeanDistance() {
        // distancias 10 y 30 -> media 20 -> afinidad 80
        assertEquals(80.0, AffinityCalculator.affinity(Map.of("ECON", 70, "WELFARE", 50), PARTY, null), 1e-9);
    }

    @Test
    void weightsChangeTheResult() {
        // ECON pesa 3 veces más: (3*10 + 1*30) / 4 = 15 -> 85
        assertEquals(85.0, AffinityCalculator.affinity(Map.of("ECON", 70, "WELFARE", 50), PARTY, Map.of("ECON", 3.0)), 1e-9);
    }

    @Test
    void zeroWeightIgnoresAxis() {
        assertEquals(90.0, AffinityCalculator.affinity(Map.of("ECON", 70, "WELFARE", 50), PARTY, Map.of("WELFARE", 0.0)), 1e-9);
    }

    @Test
    void axesMissingInThePartyAreIgnored() {
        assertEquals(100.0, AffinityCalculator.affinity(Map.of("ECON", 80, "CHANGE", 0), PARTY, null), 1e-9);
    }

    @Test
    void noCommonAxesGives0() {
        assertEquals(0.0, AffinityCalculator.affinity(Map.of("CHANGE", 50), PARTY, null), 1e-9);
    }
}
