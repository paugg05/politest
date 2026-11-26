package com.politest.politest.events.internal;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Publishes the events waiting in the outbox to Kafka.
 *
 * <ul>
 *   <li><b>Order:</b> events are sent oldest first, and the batch stops at the first failure so that a later
 *       event never overtakes an earlier one.</li>
 *   <li><b>Delivery:</b> an event is marked as published only after the broker has acknowledged it. If the
 *       application dies in between, the event is sent again: delivery is at-least-once and consumers must be
 *       idempotent (the event id is the key).</li>
 *   <li><b>Kafka down:</b> events simply stay in the outbox and are sent when the broker comes back.</li>
 * </ul>
 *
 * <p>This is a polling publisher. With several instances of the application two relays could pick the same
 * rows; the duplicates are harmless because of idempotent consumers, and {@code SELECT ... FOR UPDATE SKIP
 * LOCKED} would avoid them if that mattered.
 */
@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private static final int BATCH_SIZE = 50;
    private static final long SEND_TIMEOUT_SECONDS = 5;
    private static final Duration RETENTION = Duration.ofDays(7);

    private final OutboxStore store;
    private final KafkaTemplate<String, String> kafka;

    public OutboxRelay(OutboxStore store, KafkaTemplate<String, String> kafka) {
        this.store = store;
        this.kafka = kafka;
    }

    @Scheduled(fixedDelayString = "${app.events.relay-delay-ms:2000}")
    public void publishPending() {
        List<OutboxRecord> pending = store.pending(BATCH_SIZE);
        for (OutboxRecord event : pending) {
            try {
                kafka.send(event.topic(), event.key(), event.payload()).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                store.markPublished(event.seq());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.warn("Could not publish outbox event {} to {}; it stays pending and will be retried: {}",
                        event.key(), event.topic(), e.toString());
                return;
            }
        }
    }

    /** Published events are only kept for a while, to keep the table small. */
    @Scheduled(cron = "0 17 * * * *")
    public void purgePublished() {
        int removed = store.deletePublishedBefore(Instant.now().minus(RETENTION));
        if (removed > 0) {
            log.info("Removed {} published outbox events older than {}", removed, RETENTION);
        }
    }
}
