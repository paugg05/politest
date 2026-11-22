package com.politest.politest.affinity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.politest.politest.figures.internal.FigureServiceClient;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** El cliente de personajes no debe lanzar excepciones si el microservicio no está disponible. */
class FigureServiceClientTests {

    @Test
    void returnsEmptyWhenServiceIsDown() {
        FigureServiceClient client = new FigureServiceClient(true, "http://localhost:1", 200, 500);
        assertTrue(client.compare(Map.of("ECON", 50), "es").isEmpty());
    }

    @Test
    void returnsEmptyWhenDisabled() {
        FigureServiceClient client = new FigureServiceClient(false, "http://localhost:8091", 200, 500);
        assertTrue(client.compare(Map.of("ECON", 50), "es").isEmpty());
    }
}
