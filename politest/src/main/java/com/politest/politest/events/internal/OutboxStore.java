package com.politest.politest.events.internal;

import java.time.Instant;
import java.util.List;

/** Storage of the outbox. The only implementation talks to MySQL; tests use an in-memory one. */
public interface OutboxStore {

    /** Adds an event to be published. If a business change is stored too, call this in the same transaction. */
    void add(String topic, String key, String payload);

    /** Oldest unpublished events first. */
    List<OutboxRecord> pending(int limit);

    void markPublished(long seq);

    /** Removes events published before the cutoff and returns how many were removed. */
    int deletePublishedBefore(Instant cutoff);
}
