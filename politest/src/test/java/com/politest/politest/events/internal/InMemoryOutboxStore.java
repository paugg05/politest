package com.politest.politest.events.internal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Test double for the outbox: keeps events in memory, in insertion order. */
class InMemoryOutboxStore implements OutboxStore {

    private final List<OutboxRecord> all = new ArrayList<>();
    private final List<Long> published = new ArrayList<>();
    private long nextSeq = 1;

    @Override
    public void add(String topic, String key, String payload) {
        all.add(new OutboxRecord(nextSeq++, topic, key, payload));
    }

    @Override
    public List<OutboxRecord> pending(int limit) {
        return all.stream().filter(e -> !published.contains(e.seq())).limit(limit).toList();
    }

    @Override
    public void markPublished(long seq) {
        published.add(seq);
    }

    @Override
    public int deletePublishedBefore(Instant cutoff) {
        return 0;
    }

    int pendingCount() {
        return pending(Integer.MAX_VALUE).size();
    }
}
