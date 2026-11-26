package com.politest.politest.events.internal;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** {@link OutboxStore} on the table {@code outbox_event}. All timestamps are stored in UTC. */
@Repository
public class JdbcOutboxStore implements OutboxStore {

    private final JdbcClient jdbc;

    public JdbcOutboxStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void add(String topic, String key, String payload) {
        jdbc.sql("""
                        INSERT INTO outbox_event (id, topic, message_key, payload, created_at)
                        VALUES (:id, :topic, :key, :payload, :createdAt)""")
                .param("id", UUID.randomUUID().toString())
                .param("topic", topic)
                .param("key", key)
                .param("payload", payload)
                .param("createdAt", utcNow())
                .update();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxRecord> pending(int limit) {
        return jdbc.sql("""
                        SELECT seq, topic, message_key, payload
                        FROM outbox_event
                        WHERE published_at IS NULL
                        ORDER BY seq
                        LIMIT :limit""")
                .param("limit", limit)
                .query((rs, rowNum) -> new OutboxRecord(
                        rs.getLong("seq"), rs.getString("topic"), rs.getString("message_key"), rs.getString("payload")))
                .list();
    }

    @Override
    @Transactional
    public void markPublished(long seq) {
        jdbc.sql("UPDATE outbox_event SET published_at = :at WHERE seq = :seq")
                .param("at", utcNow())
                .param("seq", seq)
                .update();
    }

    @Override
    @Transactional
    public int deletePublishedBefore(Instant cutoff) {
        return jdbc.sql("DELETE FROM outbox_event WHERE published_at IS NOT NULL AND published_at < :cutoff")
                .param("cutoff", LocalDateTime.ofInstant(cutoff, ZoneOffset.UTC))
                .update();
    }

    private static LocalDateTime utcNow() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
