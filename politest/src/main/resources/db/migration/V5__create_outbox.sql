-- V5: transactional outbox for domain events.
-- Events are first stored here and a relay publishes them to Kafka, so an event is not lost when the
-- broker is unavailable. The payload is the anonymous event JSON (no personal data) and published rows
-- are purged after a retention period.

CREATE TABLE outbox_event (
    seq          BIGINT        NOT NULL AUTO_INCREMENT,
    id           CHAR(36)      NOT NULL,
    topic        VARCHAR(100)  NOT NULL,
    message_key  VARCHAR(100)  NOT NULL,
    payload      VARCHAR(4000) NOT NULL,
    created_at   DATETIME(3)   NOT NULL,
    published_at DATETIME(3)   NULL,
    PRIMARY KEY (seq),
    UNIQUE KEY uk_outbox_event_id (id),
    KEY idx_outbox_event_pending (published_at, seq)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
