package com.politest.figure.domain.model;

import com.politest.figure.domain.error.DomainValidationException;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Aggregate root: a deceased historical figure and everything known about their positions.
 *
 * <p>Invariants: name required, birth not after death, at most one position per axis, and positions are always
 * kept in axis order. Positions can only be reached through the aggregate. Texts are in a single language,
 * the one the figure was loaded with.
 */
public final class Figure {

    private final FigureCode code;
    private final String name;
    private final String description;
    private final int birthYear;
    private final int deathYear;
    private final String color;
    private final List<Position> positions;

    public Figure(FigureCode code, String name, String description, int birthYear, int deathYear, String color,
                  List<Position> positions) {
        if (code == null || name == null || name.isBlank()) {
            throw new DomainValidationException("Un personaje necesita código y nombre.");
        }
        if (birthYear > deathYear) {
            throw new DomainValidationException(
                    "El personaje " + code.value() + " no puede morir antes de nacer: " + birthYear + " - " + deathYear);
        }
        Map<AxisCode, Position> byAxis = new EnumMap<>(AxisCode.class);
        for (Position position : positions) {
            if (byAxis.put(position.axis(), position) != null) {
                throw new DomainValidationException(
                        "El personaje " + code.value() + " tiene dos posiciones para el eje " + position.axis());
            }
        }
        this.code = code;
        this.name = name;
        this.description = description;
        this.birthYear = birthYear;
        this.deathYear = deathYear;
        this.color = color;
        this.positions = byAxis.values().stream().sorted(Comparator.comparing(Position::axis)).toList();
    }

    public FigureCode code() {
        return code;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public int birthYear() {
        return birthYear;
    }

    public int deathYear() {
        return deathYear;
    }

    public String color() {
        return color;
    }

    /** Positions ordered by axis. */
    public List<Position> positions() {
        return positions;
    }

    public Optional<Position> positionOn(AxisCode axis) {
        return positions.stream().filter(position -> position.axis() == axis).findFirst();
    }
}
