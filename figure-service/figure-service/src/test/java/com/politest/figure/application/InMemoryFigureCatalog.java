package com.politest.figure.application;

import com.politest.figure.application.port.out.FigureCatalog;
import com.politest.figure.domain.model.DatasetInfo;
import com.politest.figure.domain.model.Figure;
import com.politest.figure.domain.model.FigureWithPositions;
import com.politest.figure.domain.model.Position;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Test double for the {@link FigureCatalog} port: no database needed. Texts are tagged with the requested language. */
class InMemoryFigureCatalog implements FigureCatalog {

    private final List<FigureWithPositions> figures;

    InMemoryFigureCatalog(FigureWithPositions... figures) {
        this.figures = List.of(figures);
    }

    static FigureWithPositions figure(String code, int econ, int welfare) {
        return new FigureWithPositions(new Figure(code, "Figure " + code, "Description of " + code, 1900, 1980, "#663399"),
                List.of(new Position("ECON", econ, 2, "note"), new Position("WELFARE", welfare, 3, "note")));
    }

    @Override
    public List<FigureWithPositions> findAllActive(String lang) {
        return figures.stream()
                .map(f -> new FigureWithPositions(
                        new Figure(f.figure().code(), f.figure().name() + " [" + lang + "]", f.figure().description(),
                                f.figure().birthYear(), f.figure().deathYear(), f.figure().color()),
                        f.positions()))
                .toList();
    }

    @Override
    public Optional<FigureWithPositions> findByCode(String code, String lang) {
        return findAllActive(lang).stream().filter(f -> f.figure().code().equalsIgnoreCase(code)).findFirst();
    }

    @Override
    public DatasetInfo datasetInfo() {
        return new DatasetInfo("test", "PROVISIONAL", "test data", LocalDate.of(2026, 1, 1));
    }
}
