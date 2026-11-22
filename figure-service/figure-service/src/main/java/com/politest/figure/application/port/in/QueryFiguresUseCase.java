package com.politest.figure.application.port.in;

import com.politest.figure.domain.model.DatasetInfo;
import com.politest.figure.domain.model.FigureWithPositions;
import java.util.List;
import java.util.Optional;

/** Inbound port: read the catalogue of historical figures. */
public interface QueryFiguresUseCase {

    Catalog listFigures(String lang);

    Optional<FigureWithPositions> findFigure(String code, String lang);

    record Catalog(DatasetInfo dataset, List<FigureWithPositions> figures) {
    }
}
