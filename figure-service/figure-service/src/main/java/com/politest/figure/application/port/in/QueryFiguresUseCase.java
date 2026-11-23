package com.politest.figure.application.port.in;

import com.politest.figure.domain.model.DatasetInfo;
import com.politest.figure.domain.model.Figure;
import java.util.List;
import java.util.Optional;

/** Inbound port: read the catalogue of historical figures. */
public interface QueryFiguresUseCase {

    /** @param lang {@code es}, {@code ca} or {@code en} */
    Catalog listFigures(String lang);

    /** Finds a figure by code, ignoring case. An invalid code simply finds nothing. */
    Optional<Figure> findFigure(String code, String lang);

    record Catalog(DatasetInfo dataset, List<Figure> figures) {
    }
}
