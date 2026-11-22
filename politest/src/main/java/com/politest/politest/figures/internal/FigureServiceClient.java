package com.politest.politest.figures.internal;

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
 * Cliente HTTP del figure-service. Si el servicio no responde, devuelve vacío: la web sigue funcionando
 * y simplemente no muestra la sección de personajes (degradación elegante).
 */
@Component
public class FigureServiceClient {

    private static final Logger log = LoggerFactory.getLogger(FigureServiceClient.class);

    private final boolean enabled;
    private final RestClient client;

    public FigureServiceClient(
            @Value("${app.figure-service.enabled:true}") boolean enabled,
            @Value("${app.figure-service.url:http://localhost:8091}") String baseUrl,
            @Value("${app.figure-service.connect-timeout-ms:500}") long connectTimeoutMs,
            @Value("${app.figure-service.read-timeout-ms:2000}") long readTimeoutMs) {
        this.enabled = enabled;
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofMillis(connectTimeoutMs)).build());
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    /** Pide la comparación en el idioma indicado. Vacío si el servicio está desactivado o no disponible. */
    public Optional<FigureApi.Response> compare(Map<String, Integer> scores, String lang) {
        if (!enabled) {
            return Optional.empty();
        }
        try {
            FigureApi.Response response = client.post()
                    .uri(uri -> uri.path("/api/v1/affinity").queryParam("lang", lang).build())
                    .body(Map.of("scores", scores))
                    .retrieve()
                    .body(FigureApi.Response.class);
            return Optional.ofNullable(response);
        } catch (RuntimeException e) {
            log.warn("figure-service no disponible: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
