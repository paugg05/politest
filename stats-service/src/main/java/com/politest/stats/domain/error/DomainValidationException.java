package com.politest.stats.domain.error;

/** Raised when a value or an aggregate would break a domain invariant. */
public class DomainValidationException extends RuntimeException {

    public DomainValidationException(String message) {
        super(message);
    }
}
