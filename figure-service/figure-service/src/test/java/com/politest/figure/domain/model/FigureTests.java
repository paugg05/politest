package com.politest.figure.domain.model;

import static com.politest.figure.domain.TestFigures.position;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.politest.figure.domain.error.DomainValidationException;
import java.util.List;
import org.junit.jupiter.api.Test;

/** The Figure aggregate protects its own invariants. */
class FigureTests {

    private static Figure figure(int birth, int death, List<Position> positions) {
        return new Figure(FigureCode.of("TEST"), "Test", "Description", birth, death, "#000000", positions);
    }

    @Test
    void positionsAreKeptInAxisOrder() {
        Figure figure = figure(1900, 1980,
                List.of(position(AxisCode.CHANGE, 10), position(AxisCode.ECON, 20), position(AxisCode.NATION, 30)));

        List<AxisCode> axes = figure.positions().stream().map(Position::axis).toList();
        assertEquals(List.of(AxisCode.ECON, AxisCode.NATION, AxisCode.CHANGE), axes);
    }

    @Test
    void rejectsTwoPositionsOnTheSameAxis() {
        assertThrows(DomainValidationException.class,
                () -> figure(1900, 1980, List.of(position(AxisCode.ECON, 10), position(AxisCode.ECON, 90))));
    }

    @Test
    void cannotDieBeforeBeingBorn() {
        assertThrows(DomainValidationException.class, () -> figure(1980, 1900, List.of()));
        assertEquals(1900, figure(1900, 1900, List.of()).deathYear());
    }

    @Test
    void requiresACodeAndANonBlankName() {
        assertThrows(DomainValidationException.class,
                () -> new Figure(null, "Test", "d", 1900, 1980, "#000000", List.of()));
        assertThrows(DomainValidationException.class,
                () -> new Figure(FigureCode.of("TEST"), " ", "d", 1900, 1980, "#000000", List.of()));
    }

    @Test
    void positionsCannotBeModifiedFromOutside() {
        Figure figure = figure(1900, 1980, List.of(position(AxisCode.ECON, 10)));

        assertThrows(UnsupportedOperationException.class, () -> figure.positions().clear());
    }

    @Test
    void findsAPositionByAxis() {
        Figure figure = figure(1900, 1980, List.of(position(AxisCode.ECON, 10)));

        assertTrue(figure.positionOn(AxisCode.ECON).isPresent());
        assertTrue(figure.positionOn(AxisCode.CHANGE).isEmpty());
    }
}
