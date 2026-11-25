package com.politest.stats.adapter.in.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.politest.stats.application.port.in.RegisterCompletedTestUseCase;
import com.politest.stats.application.port.in.RegisterCompletedTestUseCase.Command;
import com.politest.stats.application.port.in.RegisterCompletedTestUseCase.Outcome;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TestCompletedListenerTests {

    private static final String PAYLOAD = """
            {"eventId":"3f2b8c1e-5d4a-4c3b-9a7e-1b2c3d4e5f60",
             "occurredAt":"2026-11-25T14:30:00Z",
             "language":"ca",
             "rightPct":{"ECON":70,"LIBERTY":20}}""";

    private final List<Command> received = new ArrayList<>();
    private final RegisterCompletedTestUseCase useCase = command -> {
        received.add(command);
        return Outcome.REGISTERED;
    };
    private final TestCompletedListener listener = new TestCompletedListener(useCase);

    @Test
    void translatesTheJsonPayloadIntoACommand() {
        listener.onMessage(PAYLOAD);

        assertEquals(1, received.size());
        Command command = received.get(0);
        assertEquals("3f2b8c1e-5d4a-4c3b-9a7e-1b2c3d4e5f60", command.eventId());
        assertEquals("2026-11-25T14:30:00Z", command.occurredAt());
        assertEquals("ca", command.language());
        assertEquals(Map.of("ECON", 70, "LIBERTY", 20), command.rightPct());
    }

    @Test
    void ignoresFieldsAddedByTheProducer() {
        String withExtraField = PAYLOAD.replace("\"language\":\"ca\",", "\"language\":\"ca\",\"schemaVersion\":2,");

        listener.onMessage(withExtraField);

        assertEquals(1, received.size());
    }

    @Test
    void malformedJsonFailsSoTheErrorHandlerCanAct() {
        assertThrows(RuntimeException.class, () -> listener.onMessage("{not json"));
        assertTrue(received.isEmpty());
    }

    @Test
    void topicNameMatchesTheProducerContract() {
        assertEquals("test.completed", TestCompletedMessage.TOPIC);
    }
}
