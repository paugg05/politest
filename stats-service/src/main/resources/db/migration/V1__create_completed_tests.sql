CREATE TABLE completed_test (
    event_id    CHAR(36)     NOT NULL,
    occurred_at TIMESTAMP(3) NOT NULL,
    language    CHAR(2)      NOT NULL,
    received_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (event_id)
);

CREATE TABLE completed_test_score (
    event_id  CHAR(36)    NOT NULL,
    axis      VARCHAR(20) NOT NULL,
    right_pct TINYINT UNSIGNED NOT NULL,
    PRIMARY KEY (event_id, axis),
    CONSTRAINT fk_completed_test_score_test
        FOREIGN KEY (event_id) REFERENCES completed_test (event_id) ON DELETE CASCADE
);
