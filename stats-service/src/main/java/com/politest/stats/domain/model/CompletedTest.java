package com.politest.stats.domain.model;

import com.politest.stats.domain.error.DomainValidationException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * A questionnaire that somebody finished: when, in which language and the resulting position on each axis.
 * It carries no personal data. Immutable; the event id is its identity.
 */
public record CompletedTest(EventId id, Instant occurredAt, Language language, Map<AxisCode, Percentage> scores) {

    public CompletedTest {
        if (id == null || occurredAt == null || language == null || scores == null) {
            throw new DomainValidationException("Un test completado necesita id, fecha, idioma y puntuaciones.");
        }
        if (scores.isEmpty()) {
            throw new DomainValidationException("Un test completado necesita al menos una puntuación.");
        }
        scores = Collections.unmodifiableMap(new EnumMap<>(scores));
    }

    /** Builds a completed test from the raw values of an event, validating each one. */
    public static CompletedTest of(String eventId, String occurredAt, String language, Map<String, Integer> rightPct) {
        if (rightPct == null) {
            throw new DomainValidationException("Un test completado necesita al menos una puntuación.");
        }
        Map<AxisCode, Percentage> scores = new EnumMap<>(AxisCode.class);
        rightPct.forEach((axis, value) -> {
            if (value == null) {
                throw new DomainValidationException("Falta la puntuación del eje " + axis);
            }
            scores.put(AxisCode.of(axis), Percentage.of(value));
        });
        return new CompletedTest(EventId.of(eventId), parseInstant(occurredAt), Language.of(language), scores);
    }

    private static Instant parseInstant(String text) {
        if (text == null) {
            throw new DomainValidationException("La fecha del evento es obligatoria.");
        }
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException e) {
            throw new DomainValidationException("Fecha de evento no válida: " + text);
        }
    }
}
