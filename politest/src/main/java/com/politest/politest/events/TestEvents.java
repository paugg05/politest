package com.politest.politest.events;

import com.politest.politest.events.internal.TestCompletedEvent;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Public API of the "events" module: publishes business events to Kafka.
 *
 * <p>Publishing is best effort and never blocks or breaks the user's request: if Kafka is down the
 * statistics miss one event, but the person still sees their result.
 */
@Component
public class TestEvents {

    public static final String TEST_COMPLETED_TOPIC = "test.completed";

    private static final Logger log = LoggerFactory.getLogger(TestEvents.class);

    private final KafkaTemplate<String, String> kafka;

    public TestEvents(KafkaTemplate<String, String> kafka) {
        this.kafka = kafka;
    }

    /**
     * Announces that a questionnaire was finished. The event id is also the record key.
     * The timestamp is truncated to the day: statistics do not need more, and a precise time
     * could be matched against server logs to single out one person.
     */
    public void testCompleted(Map<String, Integer> rightPct, String language) {
        try {
            TestCompletedEvent event = TestCompletedEvent.of(UUID.randomUUID(), Instant.now().truncatedTo(ChronoUnit.DAYS), language, rightPct);
            kafka.send(TEST_COMPLETED_TOPIC, event.eventId(), event.toJson())
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.warn("Could not publish {} event {}: {}", TEST_COMPLETED_TOPIC, event.eventId(), error.toString());
                        }
                    });
        } catch (RuntimeException e) {
            log.warn("Could not publish {} event: {}", TEST_COMPLETED_TOPIC, e.toString());
        }
    }
}
