package com.politest.figure.application.service;

import com.politest.figure.application.InvalidAffinityRequestException;
import com.politest.figure.application.port.in.CompareAffinityUseCase;
import com.politest.figure.application.port.in.QueryFiguresUseCase;
import com.politest.figure.application.port.out.FigureCatalog;
import com.politest.figure.domain.AffinityCalculator;
import com.politest.figure.domain.model.Axes;
import com.politest.figure.domain.model.FigureWithPositions;
import com.politest.figure.domain.model.Position;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implements the use cases of the service. It only depends on ports and on the domain.
 *
 * <p>Axes on which a figure has no position (because they were not an issue in their time) are ignored.
 */
public class AffinityService implements CompareAffinityUseCase, QueryFiguresUseCase {

    private final FigureCatalog catalog;

    public AffinityService(FigureCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public Result compare(Query query, String lang) {
        validate(query, lang);
        Map<String, Integer> scores = query.scores();

        List<FigureMatch> matches = catalog.findAllActive(lang).stream()
                .map(figure -> toMatch(figure, scores, query.weights()))
                .sorted(Comparator.comparingInt(FigureMatch::affinity).reversed())
                .toList();
        return new Result(catalog.datasetInfo(), matches);
    }

    @Override
    public Catalog listFigures(String lang) {
        return new Catalog(catalog.datasetInfo(), catalog.findAllActive(lang));
    }

    @Override
    public Optional<FigureWithPositions> findFigure(String code, String lang) {
        return catalog.findByCode(code, lang);
    }

    private static FigureMatch toMatch(FigureWithPositions item, Map<String, Integer> scores,
                                       Map<String, Double> weights) {
        double affinity = AffinityCalculator.affinity(scores, item.asMap(), weights);
        List<AxisGap> gaps = item.positions().stream()
                .filter(position -> scores.containsKey(position.axis()))
                .map(position -> toGap(position, scores.get(position.axis())))
                .toList();
        var figure = item.figure();
        return new FigureMatch(figure.code(), figure.name(), figure.description(), figure.birthYear(),
                figure.deathYear(), figure.color(), (int) Math.round(affinity), gaps);
    }

    private static AxisGap toGap(Position position, int user) {
        return new AxisGap(position.axis(), user, position.rightPct(), position.confidence(), position.note());
    }

    private static void validate(Query query, String lang) {
        if (!Axes.LANGS.contains(lang)) {
            throw new InvalidAffinityRequestException("Idioma no soportado: " + lang + " (usa es, ca o en).");
        }
        if (query == null || query.scores() == null || query.scores().isEmpty()) {
            throw new InvalidAffinityRequestException("Falta «scores»: porcentajes por eje.");
        }
        for (Map.Entry<String, Integer> entry : query.scores().entrySet()) {
            if (!Axes.CODES.contains(entry.getKey())) {
                throw new InvalidAffinityRequestException("Eje desconocido: " + entry.getKey());
            }
            if (entry.getValue() == null || entry.getValue() < 0 || entry.getValue() > 100) {
                throw new InvalidAffinityRequestException(
                        "El porcentaje de " + entry.getKey() + " debe estar entre 0 y 100.");
            }
        }
        if (query.weights() != null) {
            for (Map.Entry<String, Double> entry : query.weights().entrySet()) {
                if (!Axes.CODES.contains(entry.getKey())) {
                    throw new InvalidAffinityRequestException("Eje desconocido en «weights»: " + entry.getKey());
                }
                if (entry.getValue() == null || entry.getValue() < 0 || entry.getValue() > 10) {
                    throw new InvalidAffinityRequestException(
                            "El peso de " + entry.getKey() + " debe estar entre 0 y 10.");
                }
            }
        }
    }
}
