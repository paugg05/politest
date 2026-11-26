package com.politest.politest.events;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.politest.politest.events.internal.OutboxRecord;
import com.politest.politest.events.internal.OutboxStore;
import com.politest.politest.events.internal.TestCompletedEvent;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TestEventsTests {

    /** Records what is added to the outbox. */
    private static class RecordingStore implements OutboxStore {
        final List<String[]> added = new ArrayList<>();

        @Override
        public void add(String topic, String key, String payload) {
            added.add(new String[] {topic, key, payload});
        }

        @Override
        public List<OutboxRecord> pending(int limit) {
            return List.of();
        }

        @Override
        public void markPublished(long seq) {
        }

        @Override
        public int deletePublishedBefore(Instant cutoff) {
            return 0;
        }
    }

    @Test
    void recordsOneEventInTheOutboxKeyedByItsId() {
        RecordingStore store = new RecordingStore();

        new TestEvents(store).testCompleted(Map.of("ECON", 70), "es");

        assertEquals(1, store.added.size());
        String[] entry = store.added.get(0);
        assertEquals("test.completed", entry[0]);
        TestCompletedEvent event = TestCompletedEvent.fromJson(entry[2]);
        assertEquals(event.eventId(), entry[1]);
        assertEquals("es", event.language());
        assertEquals(Map.of("ECON", 70), event.rightPct());
    }

    @Test
    void theTimestampIsTruncatedToTheDay() {
        RecordingStore store = new RecordingStore();

        new TestEvents(store).testCompleted(Map.of("ECON", 70), "es");

        Instant occurredAt = Instant.parse(TestCompletedEvent.fromJson(store.added.get(0)[2]).occurredAt());
        assertEquals(occurredAt.truncatedTo(ChronoUnit.DAYS), occurredAt);
    }

    @Test
    void aFailingDatabaseNeverBreaksTheCaller() {
        OutboxStore broken = new RecordingStore() {
            @Override
            public void add(String topic, String key, String payload) {
                throw new IllegalStateException("database down");
            }
        };

        assertDoesNotThrow(() -> new TestEvents(broken).testCompleted(Map.of("ECON", 70), "es"));
    }

    @Test
    void eachCallProducesADistinctEvent() {
        RecordingStore store = new RecordingStore();
        TestEvents events = new TestEvents(store);

        events.testCompleted(Map.of("ECON", 70), "es");
        events.testCompleted(Map.of("ECON", 70), "es");

        assertEquals(2, store.added.size());
        assertTrue(!store.added.get(0)[1].equals(store.added.get(1)[1]));
    }
}
