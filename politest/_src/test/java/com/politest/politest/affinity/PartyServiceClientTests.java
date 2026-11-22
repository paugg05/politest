package com.politest.politest.affinity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.politest.politest.affinity.internal.PartyServiceClient;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** El cliente no debe lanzar excepciones si el microservicio no está disponible. */
class PartyServiceClientTests {

    @Test
    void returnsEmptyWhenServiceIsDown() {
        PartyServiceClient client = new PartyServiceClient(true, "http://localhost:1", 200, 500);
        assertTrue(client.compare(Map.of("ECON", 50)).isEmpty());
    }

    @Test
    void returnsEmptyWhenDisabled() {
        PartyServiceClient client = new PartyServiceClient(false, "http://localhost:8090", 200, 500);
        assertTrue(client.compare(Map.of("ECON", 50)).isEmpty());
    }
}
