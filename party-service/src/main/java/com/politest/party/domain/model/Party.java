package com.politest.party.domain.model;

import com.politest.party.domain.error.DomainValidationException;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Aggregate root: a party and everything known about its positions.
 *
 * <p>Invariants: at most one position per axis, and positions are always kept in axis order.
 * Positions can only be reached through the aggregate.
 */
public final class Party {

    private final PartyCode code;
    private final String name;
    private final String color;
    private final List<Position> positions;

    public Party(PartyCode code, String name, String color, List<Position> positions) {
        if (code == null || name == null || name.isBlank()) {
            throw new DomainValidationException("Un partido necesita código y nombre.");
        }
        Map<AxisCode, Position> byAxis = new EnumMap<>(AxisCode.class);
        for (Position position : positions) {
            if (byAxis.put(position.axis(), position) != null) {
                throw new DomainValidationException(
                        "El partido " + code.value() + " tiene dos posiciones para el eje " + position.axis());
            }
        }
        this.code = code;
        this.name = name;
        this.color = color;
        this.positions = byAxis.values().stream().sorted(Comparator.comparing(Position::axis)).toList();
    }

    public PartyCode code() {
        return code;
    }

    public String name() {
        return name;
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
