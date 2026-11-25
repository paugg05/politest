package com.politest.stats.adapter.in.messaging;

import com.politest.stats.application.port.in.RegisterCompletedTestUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Inbound adapter: consumes {@code test.completed} events from Kafka and hands them to the use case.
 *
 * <p>Delivery is at-least-once, so the same event may arrive twice; the use case is idempotent.
 * If processing throws, Spring Kafka's default error handler retries and then skips the record.
 */
@Component
class TestCompletedListener {

    private static final Logger log = LoggerFactory.getLogger(TestCompletedListener.class);

    private final RegisterCompletedTestUseCase registerCompletedTest;

    TestCompletedListener(RegisterCompletedTestUseCase registerCompletedTest) {
        this.registerCompletedTest = registerCompletedTest;
    }

    @KafkaListener(topics = TestCompletedMessage.TOPIC)
    void onMessage(String payload) {
        TestCompletedMessage message = TestCompletedMessage.fromJson(payload);
        RegisterCompletedTestUseCase.Outcome outcome = registerCompletedTest.register(message.toCommand());
        log.info("Event {} processed: {}", message.eventId(), outcome);
    }
}
