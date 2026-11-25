package com.politest.stats.adapter.in.messaging;

/** The payload of a record is not a readable event. Retrying can never fix it, so it is not retried. */
class MalformedEventException extends RuntimeException {

    MalformedEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
