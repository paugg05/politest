package com.politest.figure.application;

import com.politest.figure.application.port.out.FigureCatalog;
import com.politest.figure.domain.model.DatasetInfo;
import com.politest.figure.domain.model.Figure;
import com.politest.figure.domain.model.FigureCode;
import com.politest.figure.domain.model.Language;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Test double for the {@link FigureCatalog} port: no database needed. Names are tagged with the requested language. */
class InMemoryFigureCatalog implements FigureCatalog {

    private final List<Figure> figures;

    InMemoryFigureCatalog(Figure... figures) {
        this.figures = List.of(figures);
    }

    @Override
    public List<Figure> findAllActive(Language language) {
        return figures.stream()
                .map(f -> new Figure(f.code(), f.name() + " [" + language.code() + "]", f.description(),
                        f.birthYear(), f.deathYear(), f.color(), f.positions()))
                .toList();
    }

    @Override
    public Optional<Figure> findByCode(FigureCode code, Language language) {
        return findAllActive(language).stream().filter(f -> f.code().equals(code)).findFirst();
    }

    @Override
    public DatasetInfo datasetInfo() {
        return new DatasetInfo("test", "PROVISIONAL", "test data", LocalDate.of(2026, 1, 1));
    }
}
