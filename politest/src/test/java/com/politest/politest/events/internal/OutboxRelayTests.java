package com.politest.politest.events.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

class OutboxRelayTests {

    private InMemoryOutboxStore store;
    private KafkaTemplate<String, String> kafka;
    private OutboxRelay relay;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        store = new InMemoryOutboxStore();
        kafka = mock(KafkaTemplate.class);
        relay = new OutboxRelay(store, kafka);
    }

    private static CompletableFuture<SendResult<String, String>> acknowledged() {
        return CompletableFuture.completedFuture(null);
    }

    private static CompletableFuture<SendResult<String, String>> failed() {
        return CompletableFuture.failedFuture(new IllegalStateException("broker unavailable"));
    }

    @Test
    void publishesPendingEventsAndMarksThem() {
        store.add("test.completed", "id-1", "{\"n\":1}");
        store.add("test.completed", "id-2", "{\"n\":2}");
        when(kafka.send(anyString(), anyString(), anyString())).thenReturn(acknowledged());

        relay.publishPending();

        verify(kafka).send("test.completed", "id-1", "{\"n\":1}");
        verify(kafka).send("test.completed", "id-2", "{\"n\":2}");
        assertEquals(0, store.pendingCount());
    }

    @Test
    void doesNothingWhenTheOutboxIsEmpty() {
        relay.publishPending();

        verify(kafka, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void anEventIsNotMarkedWhenTheBrokerDoesNotAcknowledgeIt() {
        store.add("test.completed", "id-1", "{\"n\":1}");
        when(kafka.send(anyString(), anyString(), anyString())).thenReturn(failed());

        relay.publishPending();

        assertEquals(1, store.pendingCount());
    }

    @Test
    void aSynchronousFailureIsAlsoKeptPending() {
        store.add("test.completed", "id-1", "{\"n\":1}");
        when(kafka.send(anyString(), anyString(), anyString())).thenThrow(new IllegalStateException("no metadata"));

        relay.publishPending();

        assertEquals(1, store.pendingCount());
    }

    @Test
    void stopsAtTheFirstFailureAndKeepsTheOrderOnRetry() {
        store.add("test.completed", "id-1", "p1");
        store.add("test.completed", "id-2", "p2");
        store.add("test.completed", "id-3", "p3");
        when(kafka.send(anyString(), anyString(), anyString()))
                .thenReturn(acknowledged())   // p1 succeeds
                .thenReturn(failed())         // p2 fails: p3 must not overtake it
                .thenReturn(acknowledged());  // later attempts succeed

        relay.publishPending();
        assertEquals(2, store.pendingCount());

        relay.publishPending();
        assertEquals(0, store.pendingCount());

        ArgumentCaptor<String> payloads = ArgumentCaptor.forClass(String.class);
        verify(kafka, org.mockito.Mockito.times(4)).send(anyString(), anyString(), payloads.capture());
        assertEquals(List.of("p1", "p2", "p2", "p3"), payloads.getAllValues());
    }
}
