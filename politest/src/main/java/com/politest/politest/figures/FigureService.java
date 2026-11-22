package com.politest.politest.figures;

import com.politest.politest.catalog.CatalogService;
import com.politest.politest.catalog.CatalogService.AxisView;
import com.politest.politest.figures.internal.FigureApi;
import com.politest.politest.figures.internal.FigureServiceClient;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * API pública del módulo "figures": compara los porcentajes de la persona con personajes históricos
 * llamando al microservicio figure-service y añade los nombres de los ejes en el idioma pedido.
 */
@Service
public class FigureService {

    /** @param matches los personajes más afines, del más al menos parecido */
    public record FigureReport(List<FigureMatch> matches) {
    }

    /** @param years por ejemplo «1925-2013» */
    public record FigureMatch(String code, String name, String description, String years, String color,
                              int affinity, List<AxisGap> gaps) {
    }

    /** Diferencia en un eje: posición de la persona y del personaje (0-100, hacia el polo derecho). */
    public record AxisGap(String axisCode, String axisName, String leftLabel, String rightLabel,
                          int user, int figure) {
    }

    private final FigureServiceClient client;
    private final CatalogService catalog;
    private final int maxResults;

    public FigureService(FigureServiceClient client, CatalogService catalog,
                         @Value("${app.figure-service.max-results:4}") int maxResults) {
        this.client = client;
        this.catalog = catalog;
        this.maxResults = maxResults;
    }

    /** Vacío si el figure-service no está disponible o no devuelve personajes. */
    public Optional<FigureReport> compare(Map<String, Integer> rightPct, String lang) {
        Map<String, AxisView> axes = catalog.axes(lang).stream()
                .collect(Collectors.toMap(AxisView::code, Function.identity()));

        return client.compare(rightPct, lang)
                .filter(response -> response.matches() != null && !response.matches().isEmpty())
                .map(response -> new FigureReport(response.matches().stream()
                        .limit(maxResults)
                        .map(m -> toMatch(m, axes))
                        .toList()));
    }

    private static FigureMatch toMatch(FigureApi.Match match, Map<String, AxisView> axes) {
        List<AxisGap> gaps = match.axes().stream()
                .filter(g -> axes.containsKey(g.axis()))
                .map(g -> {
                    AxisView axis = axes.get(g.axis());
                    return new AxisGap(g.axis(), axis.name(), axis.leftLabel(), axis.rightLabel(),
                            g.user(), g.figure());
                })
                .toList();
        return new FigureMatch(match.code(), match.name(), match.description(),
                match.birthYear() + "-" + match.deathYear(), match.color(), match.affinity(), gaps);
    }
}
