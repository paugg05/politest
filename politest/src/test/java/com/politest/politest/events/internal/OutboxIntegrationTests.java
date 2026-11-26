package com.politest.politest.events.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.politest.politest.events.TestEvents;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.mysql.MySQLContainer;

/**
 * The outbox against a real MySQL (with the real Flyway migrations) and a real Kafka broker, both started by
 * Testcontainers. It needs Docker; without it the class is skipped.
 */
@Testcontainers(disabledWithoutDocker = true)
class OutboxIntegrationTests {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka:4.0.0");

    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("politest")
            .withUsername("politest")
            .withPassword("politest");

    private static JdbcClient jdbc;
    private static JdbcOutboxStore store;

    @BeforeAll
    static void migrateDatabase() {
        Flyway.configure()
                .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                .load()
                .migrate();
        DriverManagerDataSource dataSource =
                new DriverManagerDataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
        jdbc = JdbcClient.create(dataSource);
        store = new JdbcOutboxStore(jdbc);
    }

    @Test
    void anEventIsStoredPublishedAndMarked() {
        new TestEvents(store).testCompleted(Map.of("ECON", 70, "LIBERTY", 20), "ca");
        List<OutboxRecord> pending = store.pending(10);
        assertEquals(1, pending.size());
        OutboxRecord stored = pending.get(0);

        relayTo(KAFKA.getBootstrapServers()).publishPending();

        assertEquals(0, store.pending(10).size());
        ConsumerRecord<String, String> received = readFromTopic(stored.key());
        assertNotNull(received, "the event never reached Kafka");
        assertEquals(stored.payload(), received.value());
        assertEquals("ca", TestCompletedEvent.fromJson(received.value()).language());
    }

    @Test
    void anEventRecordedWhileKafkaIsDownIsNotLost() {
        new TestEvents(store).testCompleted(Map.of("ECON", 40), "es");
        OutboxRecord stored = store.pending(10).get(0);

        // Nothing listens on port 1: the broker is unreachable, the event must stay in the outbox.
        relayTo("localhost:1").publishPending();
        assertEquals(1, store.pending(10).size());

        // The broker comes back: the same event is delivered.
        relayTo(KAFKA.getBootstrapServers()).publishPending();
        assertEquals(0, store.pending(10).size());
        assertNotNull(readFromTopic(stored.key()), "the event was lost");
    }

    @Test
    void publishedEventsArePurgedAfterTheRetentionPeriod() {
        new TestEvents(store).testCompleted(Map.of("ECON", 55), "en");
        relayTo(KAFKA.getBootstrapServers()).publishPending();
        long before = jdbc.sql("SELECT COUNT(*) FROM outbox_event").query(Long.class).single();

        int removed = store.deletePublishedBefore(Instant.now().plusSeconds(60));

        long after = jdbc.sql("SELECT COUNT(*) FROM outbox_event").query(Long.class).single();
        assertTrue(removed >= 1);
        assertEquals(before - removed, after);
    }

    private static OutboxRelay relayTo(String bootstrapServers) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 1000);
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000);
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 2000);
        KafkaTemplate<String, String> template = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(config));
        return new OutboxRelay(store, template);
    }

    /** Reads the topic from the beginning with a throwaway consumer group and returns the record with that key. */
    private static ConsumerRecord<String, String> readFromTopic(String key) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "outbox-test-" + UUID.randomUUID());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(config)) {
            consumer.subscribe(List.of(TestEvents.TEST_COMPLETED_TOPIC));
            long deadline = System.nanoTime() + TIMEOUT.toNanos();
            while (System.nanoTime() < deadline) {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) {
                    if (key.equals(record.key())) {
                        return record;
                    }
                }
            }
            return null;
        }
    }
}
