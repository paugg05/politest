package com.politest.politest.events;

import com.politest.politest.events.internal.OutboxStore;
import com.politest.politest.events.internal.TestCompletedEvent;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Public API of the "events" module: records business events so that they are published to Kafka.
 *
 * <p>Events are written to an outbox table and a relay publishes them asynchronously. The request of the
 * person taking the test never waits for Kafka, and an event is not lost if Kafka is down at that moment.
 */
@Component
public class TestEvents {

    public static final String TEST_COMPLETED_TOPIC = "test.completed";

    private static final Logger log = LoggerFactory.getLogger(TestEvents.class);

    private final OutboxStore outbox;

    public TestEvents(OutboxStore outbox) {
        this.outbox = outbox;
    }

    /**
     * Records that a questionnaire was finished. The event id is also the record key.
     * The timestamp is truncated to the day: statistics do not need more, and a precise time
     * could be matched against server logs to single out one person.
     *
     * <p>Statistics are not worth breaking the page for, so a failure is logged and swallowed.
     */
    public void testCompleted(Map<String, Integer> rightPct, String language) {
        try {
            TestCompletedEvent event = TestCompletedEvent.of(
                    UUID.randomUUID(), Instant.now().truncatedTo(ChronoUnit.DAYS), language, rightPct);
            outbox.add(TEST_COMPLETED_TOPIC, event.eventId(), event.toJson());
        } catch (RuntimeException e) {
            log.warn("Could not record the {} event: {}", TEST_COMPLETED_TOPIC, e.toString());
        }
    }
}
