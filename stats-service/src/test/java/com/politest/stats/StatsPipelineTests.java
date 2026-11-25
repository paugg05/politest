package com.politest.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.politest.stats.application.port.in.QueryStatsUseCase;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.mysql.MySQLContainer;

/**
 * End-to-end test of the consuming side with a real Kafka broker and a real MySQL database, both started by
 * Testcontainers. It needs Docker; without it the class is skipped instead of failing.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class StatsPipelineTests {

    private static final String TOPIC = "test.completed";
    private static final String DEAD_LETTER_TOPIC = "test.completed.DLT";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka:4.0.0");

    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("politest_stats")
            .withUsername("stats")
            .withPassword("stats");

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired
    KafkaTemplate<String, String> kafka;

    @Autowired
    QueryStatsUseCase stats;

    @Test
    void aValidEventEndsUpInTheStatistics() throws Exception {
        long before = stats.summary().totalTests();

        send("valid", event(UUID.randomUUID().toString(), "ca", 70));

        eventually(() -> stats.summary().totalTests() == before + 1, "the event was not stored");
    }

    @Test
    void aRedeliveredEventIsCountedOnlyOnce() throws Exception {
        long before = stats.summary().totalTests();
        String repeated = event(UUID.randomUUID().toString(), "es", 40);
        String other = event(UUID.randomUUID().toString(), "en", 60);

        // Same key, so same partition and strict order: when "other" is counted, the duplicate was already handled.
        send("duplicates", repeated);
        send("duplicates", repeated);
        send("duplicates", other);

        eventually(() -> stats.summary().totalTests() == before + 2, "the events were not processed");
        assertEquals(before + 2, stats.summary().totalTests());
    }

    @Test
    void anUnreadablePayloadGoesToTheDeadLetterTopic() throws Exception {
        long before = stats.summary().totalTests();
        String payload = "{not json " + UUID.randomUUID();

        send("malformed", payload);

        assertTrue(reachesDeadLetterTopic(payload), "the record never reached " + DEAD_LETTER_TOPIC);
        assertEquals(before, stats.summary().totalTests());
    }

    @Test
    void anEventBreakingADomainRuleGoesToTheDeadLetterTopic() throws Exception {
        long before = stats.summary().totalTests();
        String payload = event(UUID.randomUUID().toString(), "fr", 50);

        send("invalid", payload);

        assertTrue(reachesDeadLetterTopic(payload), "the record never reached " + DEAD_LETTER_TOPIC);
        assertEquals(before, stats.summary().totalTests());
    }

    private void send(String key, String payload) throws Exception {
        kafka.send(TOPIC, key, payload).get(10, TimeUnit.SECONDS);
    }

    private static String event(String id, String language, int econ) {
        return """
                {"eventId":"%s","occurredAt":"2026-11-25T00:00:00Z","language":"%s","rightPct":{"ECON":%d}}"""
                .formatted(id, language, econ);
    }

    private static void eventually(BooleanSupplier condition, String message) throws InterruptedException {
        long deadline = System.nanoTime() + TIMEOUT.toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(200);
        }
        fail(message + " within " + TIMEOUT.toSeconds() + " seconds");
    }

    /** Reads the dead-letter topic from the beginning with a throwaway consumer group. */
    private static boolean reachesDeadLetterTopic(String payload) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "dlt-reader-" + UUID.randomUUID());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(config)) {
            consumer.subscribe(List.of(DEAD_LETTER_TOPIC));
            long deadline = System.nanoTime() + TIMEOUT.toNanos();
            while (System.nanoTime() < deadline) {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) {
                    if (payload.equals(record.value())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
