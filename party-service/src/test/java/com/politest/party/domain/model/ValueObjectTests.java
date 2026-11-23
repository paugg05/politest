package com.politest.party.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.politest.party.domain.error.DomainValidationException;
import org.junit.jupiter.api.Test;

/** Value objects can only exist in a valid state. */
class ValueObjectTests {

    @Test
    void percentageAcceptsTheWholeRange() {
        assertEquals(0, Percentage.of(0).value());
        assertEquals(100, Percentage.of(100).value());
    }

    @Test
    void percentageRejectsValuesOutsideTheRange() {
        assertThrows(DomainValidationException.class, () -> Percentage.of(-1));
        assertThrows(DomainValidationException.class, () -> Percentage.of(101));
    }

    @Test
    void percentageMeasuresDistanceInBothDirections() {
        assertEquals(30, Percentage.of(20).distanceTo(Percentage.of(50)));
        assertEquals(30, Percentage.of(50).distanceTo(Percentage.of(20)));
    }

    @Test
    void valueObjectsAreComparedByValue() {
        assertEquals(Percentage.of(42), Percentage.of(42));
        assertEquals(PartyCode.of("PP"), PartyCode.of("PP"));
    }

    @Test
    void axisCodeParsesTheEightAxesAndNothingElse() {
        assertEquals(AxisCode.ECON, AxisCode.of("ECON"));
        assertEquals(8, AxisCode.values().length);
        DomainValidationException e = assertThrows(DomainValidationException.class, () -> AxisCode.of("MAGIC"));
        assertTrue(e.getMessage().contains("MAGIC"));
        assertThrows(DomainValidationException.class, () -> AxisCode.of("econ"));
    }

    @Test
    void confidenceOnlyKnowsLevelsOneToThree() {
        assertEquals(Confidence.SOLID, Confidence.ofLevel(3));
        assertEquals(1, Confidence.WEAK.level());
        assertThrows(DomainValidationException.class, () -> Confidence.ofLevel(0));
        assertThrows(DomainValidationException.class, () -> Confidence.ofLevel(4));
    }

    @Test
    void partyCodeRequiresUpperCaseLettersDigitsAndUnderscores() {
        assertEquals("EH_BILDU", PartyCode.of("EH_BILDU").value());
        assertThrows(DomainValidationException.class, () -> PartyCode.of("pp"));
        assertThrows(DomainValidationException.class, () -> PartyCode.of(""));
        assertThrows(DomainValidationException.class, () -> PartyCode.of(null));
        assertThrows(DomainValidationException.class, () -> PartyCode.of("A B"));
    }

    @Test
    void partyCodeParsingIgnoresCaseAndSpaces() {
        assertEquals(PartyCode.of("PSOE"), PartyCode.parse("  psoe "));
    }

    @Test
    void affinityIsRoundedToAWholeNumber() {
        assertEquals(67, new Affinity(66.6).rounded());
        assertEquals(100, new Affinity(100.0).rounded());
    }

    @Test
    void affinityRejectsValuesOutsideTheScale() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> new Affinity(101));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> new Affinity(-1));
    }
}
