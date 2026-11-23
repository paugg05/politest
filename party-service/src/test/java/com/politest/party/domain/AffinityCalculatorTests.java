package com.politest.party.domain;

import static com.politest.party.domain.TestParties.party;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.politest.party.domain.model.AxisScores;
import com.politest.party.domain.model.AxisWeights;
import com.politest.party.domain.model.Party;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AffinityCalculatorTests {

    private static final Party PARTY = party("TEST", 80, 20);

    private static double affinity(Map<String, Integer> scores, Map<String, Double> weights) {
        return AffinityCalculator.affinity(AxisScores.of(scores), PARTY, AxisWeights.of(weights)).value();
    }

    @Test
    void identicalPositionsGive100() {
        assertEquals(100.0, affinity(Map.of("ECON", 80, "WELFARE", 20), null), 1e-9);
    }

    @Test
    void oppositePositionsGive0() {
        Party opposite = party("OPPOSITE", 100, 0);

        double affinity = AffinityCalculator.affinity(AxisScores.of(Map.of("ECON", 0, "WELFARE", 100)), opposite,
                AxisWeights.equal()).value();

        assertEquals(0.0, affinity, 1e-9);
    }

    @Test
    void usesMeanDistance() {
        // distances 10 and 30 -> mean 20 -> affinity 80
        assertEquals(80.0, affinity(Map.of("ECON", 70, "WELFARE", 50), null), 1e-9);
    }

    @Test
    void weightsChangeTheResult() {
        // ECON weighs three times as much: (3*10 + 1*30) / 4 = 15 -> 85
        assertEquals(85.0, affinity(Map.of("ECON", 70, "WELFARE", 50), Map.of("ECON", 3.0)), 1e-9);
    }

    @Test
    void zeroWeightIgnoresAxis() {
        assertEquals(90.0, affinity(Map.of("ECON", 70, "WELFARE", 50), Map.of("WELFARE", 0.0)), 1e-9);
    }

    @Test
    void axesMissingInThePartyAreIgnored() {
        assertEquals(100.0, affinity(Map.of("ECON", 80, "CHANGE", 0), null), 1e-9);
    }

    @Test
    void noCommonAxesGives0() {
        assertEquals(0.0, affinity(Map.of("CHANGE", 50), null), 1e-9);
    }
}
