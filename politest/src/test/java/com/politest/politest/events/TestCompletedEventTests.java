package com.politest.politest.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.politest.politest.events.internal.TestCompletedEvent;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TestCompletedEventTests {

    @Test
    void jsonRoundTripKeepsEveryField() {
        UUID id = UUID.randomUUID();
        Instant at = Instant.parse("2026-11-25T14:30:00Z");
        TestCompletedEvent event = TestCompletedEvent.of(id, at, "ca", Map.of("ECON", 70, "LIBERTY", 20));

        TestCompletedEvent copy = TestCompletedEvent.fromJson(event.toJson());

        assertEquals(event, copy);
        assertEquals(id.toString(), copy.eventId());
        assertEquals("2026-11-25T14:30:00Z", copy.occurredAt());
    }

    @Test
    void jsonUsesTheFieldNamesOfTheContract() {
        String json = TestCompletedEvent.of(UUID.randomUUID(), Instant.now(), "es", Map.of("ECON", 50)).toJson();

        assertTrue(json.contains("\"eventId\""), json);
        assertTrue(json.contains("\"occurredAt\""), json);
        assertTrue(json.contains("\"language\":\"es\""), json);
        assertTrue(json.contains("\"rightPct\""), json);
    }

    @Test
    void topicNameIsTheOneConsumersListenTo() {
        assertEquals("test.completed", TestEvents.TEST_COMPLETED_TOPIC);
    }
}
