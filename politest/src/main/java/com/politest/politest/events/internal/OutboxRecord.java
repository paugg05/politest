package com.politest.politest.events.internal;

/** An event waiting in the outbox. {@code seq} gives the order in which events must be published. */
public record OutboxRecord(long seq, String topic, String key, String payload) {
}
