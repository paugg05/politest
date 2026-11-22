package com.politest.politest.affinity.internal;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP del party-service. Si el servicio no responde, devuelve vacío: la web sigue funcionando
 * y simplemente no muestra la sección de afinidad (degradación elegante).
 */
@Component
public class PartyServiceClient {

    private static final Logger log = LoggerFactory.getLogger(PartyServiceClient.class);

    private final boolean enabled;
    private final RestClient client;

    public PartyServiceClient(
            @Value("${app.party-service.enabled:true}") boolean enabled,
            @Value("${app.party-service.url:http://localhost:8090}") String baseUrl,
            @Value("${app.party-service.connect-timeout-ms:500}") long connectTimeoutMs,
            @Value("${app.party-service.read-timeout-ms:2000}") long readTimeoutMs) {
        this.enabled = enabled;
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofMillis(connectTimeoutMs)).build());
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    /** Pide la comparación. Vacío si el servicio está desactivado o no está disponible. */
    public Optional<PartyApi.Response> compare(Map<String, Integer> scores) {
        if (!enabled) {
            return Optional.empty();
        }
        try {
            PartyApi.Response response = client.post()
                    .uri("/api/v1/affinity")
                    .body(Map.of("scores", scores))
                    .retrieve()
                    .body(PartyApi.Response.class);
            return Optional.ofNullable(response);
        } catch (RuntimeException e) {
            log.warn("party-service no disponible: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
