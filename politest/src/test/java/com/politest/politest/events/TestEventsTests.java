package com.politest.politest.events;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.politest.politest.events.internal.TestCompletedEvent;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

class TestEventsTests {

    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, String> kafka = mock(KafkaTemplate.class);

    private final TestEvents events = new TestEvents(kafka);

    @Test
    void publishesOneEventToTheTopicKeyedByItsId() {
        when(kafka.send(anyString(), anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(null));

        events.testCompleted(Map.of("ECON", 70), "es");

        ArgumentCaptor<String> topic = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> value = ArgumentCaptor.forClass(String.class);
        verify(kafka).send(topic.capture(), key.capture(), value.capture());

        assertEquals("test.completed", topic.getValue());
        TestCompletedEvent event = TestCompletedEvent.fromJson(value.getValue());
        assertEquals(event.eventId(), key.getValue());
        assertEquals("es", event.language());
        assertEquals(Map.of("ECON", 70), event.rightPct());
    }

    @Test
    void aFailingBrokerNeverBreaksTheCaller() {
        when(kafka.send(anyString(), anyString(), anyString())).thenThrow(new IllegalStateException("broker down"));

        assertDoesNotThrow(() -> events.testCompleted(Map.of("ECON", 70), "es"));
    }

    @Test
    void anAsynchronousFailureIsAlsoSwallowed() {
        when(kafka.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("timeout")));

        assertDoesNotThrow(() -> events.testCompleted(Map.of("ECON", 70), "es"));
    }
}
