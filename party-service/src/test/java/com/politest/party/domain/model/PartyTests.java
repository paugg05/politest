package com.politest.party.domain.model;

import static com.politest.party.domain.TestParties.position;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.politest.party.domain.error.DomainValidationException;
import java.util.List;
import org.junit.jupiter.api.Test;

/** The Party aggregate protects its own invariants. */
class PartyTests {

    @Test
    void positionsAreKeptInAxisOrder() {
        Party party = new Party(PartyCode.of("TEST"), "Test", "#000000",
                List.of(position(AxisCode.CHANGE, 10), position(AxisCode.ECON, 20), position(AxisCode.NATION, 30)));

        List<AxisCode> axes = party.positions().stream().map(Position::axis).toList();
        assertEquals(List.of(AxisCode.ECON, AxisCode.NATION, AxisCode.CHANGE), axes);
    }

    @Test
    void rejectsTwoPositionsOnTheSameAxis() {
        assertThrows(DomainValidationException.class, () -> new Party(PartyCode.of("TEST"), "Test", "#000000",
                List.of(position(AxisCode.ECON, 10), position(AxisCode.ECON, 90))));
    }

    @Test
    void requiresACodeAndANonBlankName() {
        assertThrows(DomainValidationException.class, () -> new Party(null, "Test", "#000000", List.of()));
        assertThrows(DomainValidationException.class,
                () -> new Party(PartyCode.of("TEST"), " ", "#000000", List.of()));
    }

    @Test
    void positionsCannotBeModifiedFromOutside() {
        Party party = new Party(PartyCode.of("TEST"), "Test", "#000000", List.of(position(AxisCode.ECON, 10)));

        assertThrows(UnsupportedOperationException.class, () -> party.positions().clear());
    }

    @Test
    void findsAPositionByAxis() {
        Party party = new Party(PartyCode.of("TEST"), "Test", "#000000", List.of(position(AxisCode.ECON, 10)));

        assertTrue(party.positionOn(AxisCode.ECON).isPresent());
        assertTrue(party.positionOn(AxisCode.CHANGE).isEmpty());
    }
}
