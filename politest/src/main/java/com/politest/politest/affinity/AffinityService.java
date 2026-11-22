package com.politest.politest.affinity;

import com.politest.politest.affinity.internal.PartyApi;
import com.politest.politest.affinity.internal.PartyServiceClient;
import com.politest.politest.catalog.CatalogService;
import com.politest.politest.catalog.CatalogService.AxisView;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;


@Service
public class AffinityService {

    /** @param datasetVersion versión de los datos de partidos; @param provisional true si aún son provisionales */
    public record AffinityReport(String datasetVersion, boolean provisional, List<PartyMatch> matches) {
    }

    public record PartyMatch(String code, String name, String color, int affinity, List<AxisGap> gaps) {
    }

    /** Diferencia en un eje: posición de la persona y del partido (0-100, hacia el polo derecho). */
    public record AxisGap(String axisCode, String axisName, String leftLabel, String rightLabel,
                          int user, int party, int confidence, String note, String sourceUrl) {
    }

    private final PartyServiceClient client;
    private final CatalogService catalog;

    public AffinityService(PartyServiceClient client, CatalogService catalog) {
        this.client = client;
        this.catalog = catalog;
    }

    public Optional<AffinityReport> compare(Map<String, Integer> rightPct, String lang) {
        Map<String, AxisView> axes = catalog.axes(lang).stream()
                .collect(Collectors.toMap(AxisView::code, Function.identity()));

        return client.compare(rightPct).map(response -> new AffinityReport(
                response.dataset().version(),
                "provisional".equalsIgnoreCase(response.dataset().status()),
                response.matches().stream().map(m -> toMatch(m, axes)).toList()));
    }

    private static PartyMatch toMatch(PartyApi.Match match, Map<String, AxisView> axes) {
        List<AxisGap> gaps = match.axes().stream()
                .filter(g -> axes.containsKey(g.axis()))
                .map(g -> {
                    AxisView axis = axes.get(g.axis());
                    return new AxisGap(g.axis(), axis.name(), axis.leftLabel(), axis.rightLabel(),
                            g.user(), g.party(), g.confidence(), g.note(), g.sourceUrl());
                })
                .toList();
        return new PartyMatch(match.code(), match.name(), match.color(), match.affinity(), gaps);
    }
}
