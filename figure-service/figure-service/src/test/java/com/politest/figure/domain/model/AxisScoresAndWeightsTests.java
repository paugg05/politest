package com.politest.figure.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.politest.figure.domain.error.DomainValidationException;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AxisScoresAndWeightsTests {

    @Test
    void scoresAreBuiltFromRawValues() {
        AxisScores scores = AxisScores.of(Map.of("ECON", 40, "CHANGE", 90));

        assertEquals(40, scores.on(AxisCode.ECON).orElseThrow().value());
        assertTrue(scores.on(AxisCode.NATION).isEmpty());
        assertEquals(2, scores.axes().size());
    }

    @Test
    void scoresRejectMissingUnknownOrOutOfRangeInput() {
        assertThrows(DomainValidationException.class, () -> AxisScores.of(null));
        assertThrows(DomainValidationException.class, () -> AxisScores.of(Map.of()));
        assertThrows(DomainValidationException.class, () -> AxisScores.of(Map.of("MAGIC", 10)));
        assertThrows(DomainValidationException.class, () -> AxisScores.of(Map.of("ECON", 101)));
        assertThrows(DomainValidationException.class, () -> AxisScores.of(Map.of("ECON", -1)));
    }

    @Test
    void weightsDefaultToOne() {
        assertEquals(1.0, AxisWeights.equal().weightOf(AxisCode.ECON));
        assertEquals(1.0, AxisWeights.of(null).weightOf(AxisCode.ECON));
        assertEquals(1.0, AxisWeights.of(Map.of("CHANGE", 5.0)).weightOf(AxisCode.ECON));
        assertEquals(5.0, AxisWeights.of(Map.of("CHANGE", 5.0)).weightOf(AxisCode.CHANGE));
    }

    @Test
    void weightsRejectUnknownAxesAndValuesOutOfRange() {
        assertThrows(DomainValidationException.class, () -> AxisWeights.of(Map.of("MAGIC", 1.0)));
        assertThrows(DomainValidationException.class, () -> AxisWeights.of(Map.of("ECON", 11.0)));
        assertThrows(DomainValidationException.class, () -> AxisWeights.of(Map.of("ECON", -0.5)));
    }
}
