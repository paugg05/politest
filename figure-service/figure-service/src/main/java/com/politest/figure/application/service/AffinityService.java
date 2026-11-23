package com.politest.figure.application.service;

import com.politest.figure.application.port.in.CompareAffinityUseCase;
import com.politest.figure.application.port.in.QueryFiguresUseCase;
import com.politest.figure.application.port.out.FigureCatalog;
import com.politest.figure.domain.AffinityCalculator;
import com.politest.figure.domain.error.DomainValidationException;
import com.politest.figure.domain.model.AxisScores;
import com.politest.figure.domain.model.AxisWeights;
import com.politest.figure.domain.model.Figure;
import com.politest.figure.domain.model.FigureCode;
import com.politest.figure.domain.model.Language;
import com.politest.figure.domain.model.Position;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Implements the use cases of the service. It translates raw input into domain objects (which validate themselves),
 * orchestrates the domain and talks to the outside only through ports.
 */
public class AffinityService implements CompareAffinityUseCase, QueryFiguresUseCase {

    private final FigureCatalog catalog;

    public AffinityService(FigureCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public Result compare(Query query, String lang) {
        Language language = Language.of(lang);
        if (query == null) {
            throw new DomainValidationException("Falta «scores»: porcentajes por eje.");
        }
        AxisScores scores = AxisScores.of(query.scores());
        AxisWeights weights = AxisWeights.of(query.weights());

        List<FigureMatch> matches = catalog.findAllActive(language).stream()
                .map(figure -> toMatch(figure, scores, weights))
                .sorted(Comparator.comparingInt(FigureMatch::affinity).reversed())
                .toList();
        return new Result(catalog.datasetInfo(), matches);
    }

    @Override
    public Catalog listFigures(String lang) {
        Language language = Language.of(lang);
        return new Catalog(catalog.datasetInfo(), catalog.findAllActive(language));
    }

    @Override
    public Optional<Figure> findFigure(String code, String lang) {
        Language language = Language.of(lang);
        try {
            return catalog.findByCode(FigureCode.parse(code), language);
        } catch (DomainValidationException e) {
            return Optional.empty();
        }
    }

    private static FigureMatch toMatch(Figure figure, AxisScores scores, AxisWeights weights) {
        int affinity = AffinityCalculator.affinity(scores, figure, weights).rounded();
        List<AxisGap> gaps = figure.positions().stream()
                .filter(position -> scores.on(position.axis()).isPresent())
                .map(position -> toGap(position, scores))
                .toList();
        return new FigureMatch(figure.code().value(), figure.name(), figure.description(), figure.birthYear(),
                figure.deathYear(), figure.color(), affinity, gaps);
    }

    private static AxisGap toGap(Position position, AxisScores scores) {
        int user = scores.on(position.axis()).orElseThrow().value();
        return new AxisGap(position.axis().name(), user, position.rightPct().value(),
                position.confidence().level(), position.note());
    }
}
