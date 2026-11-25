package com.politest.stats.domain.model;

import com.politest.stats.domain.error.DomainValidationException;
import java.util.UUID;

/** Unique identifier of a business event. It is what makes processing idempotent. */
public record EventId(UUID value) {

    public EventId {
        if (value == null) {
            throw new DomainValidationException("El identificador del evento es obligatorio.");
        }
    }

    public static EventId of(String text) {
        if (text == null) {
            throw new DomainValidationException("El identificador del evento es obligatorio.");
        }
        try {
            return new EventId(UUID.fromString(text));
        } catch (IllegalArgumentException e) {
            throw new DomainValidationException("Identificador de evento no válido: " + text);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
