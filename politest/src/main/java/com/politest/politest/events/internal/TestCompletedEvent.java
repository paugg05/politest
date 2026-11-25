package com.politest.politest.events.internal;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import tools.jackson.databind.json.JsonMapper;

/**
 * Wire format of the {@code test.completed} event. It is a public contract with the consumers:
 * fields may be added, but existing ones must not change meaning.
 *
 * <p>It carries no personal data: no user, session, IP address or free text.
 *
 * @param eventId    unique id (UUID); consumers use it to process each event once
 * @param occurredAt ISO-8601 instant in UTC; the producer truncates it to the day on purpose
 * @param language   interface language the test was taken in (es, ca, en)
 * @param rightPct   axis code to position, 0 (left pole) to 100 (right pole)
 */
public record TestCompletedEvent(String eventId, String occurredAt, String language, Map<String, Integer> rightPct) {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    public static TestCompletedEvent of(UUID eventId, Instant occurredAt, String language, Map<String, Integer> rightPct) {
        return new TestCompletedEvent(eventId.toString(), occurredAt.toString(), language, Map.copyOf(rightPct));
    }

    public String toJson() {
        return JSON.writeValueAsString(this);
    }

    public static TestCompletedEvent fromJson(String json) {
        return JSON.readValue(json, TestCompletedEvent.class);
    }
}
