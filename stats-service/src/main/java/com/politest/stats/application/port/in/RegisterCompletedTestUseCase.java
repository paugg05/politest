package com.politest.stats.application.port.in;

import java.util.Map;

/** Inbound port: record a completed test received from the outside world. */
public interface RegisterCompletedTestUseCase {

    Outcome register(Command command);

    /** Raw values of the event; the domain validates them. */
    record Command(String eventId, String occurredAt, String language, Map<String, Integer> rightPct) {
    }

    enum Outcome {
        /** The test was new and has been stored. */
        REGISTERED,
        /** The same event had already been processed; nothing changed. */
        DUPLICATE
    }
}
