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
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
 *
 * <p>The broker is a fresh container on a possibly busy machine, so the "healthy" producer gets generous
 * timeouts and the topic is created and warmed up beforehand. Only the "broker down" producer uses short ones.
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
    private static KafkaTemplate<String, String> healthyKafka;

    @BeforeAll
    static void prepareInfrastructure() throws Exception {
        Flyway.configure()
                .dataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())
                .load()
                .migrate();
        jdbc = JdbcClient.create(
                new DriverManagerDataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword()));
        store = new JdbcOutboxStore(jdbc);

        try (Admin admin = Admin.create(Map.<String, Object>of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers()))) {
            admin.createTopics(List.of(new NewTopic(TestEvents.TEST_COMPLETED_TOPIC, 1, (short) 1)))
                    .all().get(30, TimeUnit.SECONDS);
        }
        healthyKafka = template(KAFKA.getBootstrapServers(), 15_000, 10_000, 20_000);
        healthyKafka.send(TestEvents.TEST_COMPLETED_TOPIC, "warm-up", "{}").get(30, TimeUnit.SECONDS);
    }

    @BeforeEach
    void emptyTheOutbox() {
        jdbc.sql("DELETE FROM outbox_event").update();
    }

    @Test
    void anEventIsStoredPublishedAndMarked() {
        new TestEvents(store).testCompleted(Map.of("ECON", 70, "LIBERTY", 20), "ca");
        List<OutboxRecord> pending = store.pending(10);
        assertEquals(1, pending.size());
        OutboxRecord stored = pending.get(0);

        new OutboxRelay(store, healthyKafka).publishPending();

        assertEquals(0, store.pending(10).size(), "the event was not marked as published");
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
        new OutboxRelay(store, template("localhost:1", 1_000, 1_000, 2_000)).publishPending();
        assertEquals(1, store.pending(10).size());

        // The broker comes back: the same event is delivered.
        new OutboxRelay(store, healthyKafka).publishPending();
        assertEquals(0, store.pending(10).size(), "the event was not published once Kafka was back");
        assertNotNull(readFromTopic(stored.key()), "the event was lost");
    }

    @Test
    void publishedEventsArePurgedAfterTheRetentionPeriod() {
        new TestEvents(store).testCompleted(Map.of("ECON", 55), "en");
        new OutboxRelay(store, healthyKafka).publishPending();
        assertEquals(0, store.pending(10).size(), "the event was not published");

        int removed = store.deletePublishedBefore(Instant.now().plusSeconds(60));

        assertTrue(removed >= 1, "no published event was purged");
        long left = jdbc.sql("SELECT COUNT(*) FROM outbox_event").query(Long.class).single();
        assertEquals(0L, left);
    }

    private static KafkaTemplate<String, String> template(String bootstrapServers, int maxBlockMs,
                                                          int requestTimeoutMs, int deliveryTimeoutMs) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, maxBlockMs);
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeoutMs);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(config));
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
