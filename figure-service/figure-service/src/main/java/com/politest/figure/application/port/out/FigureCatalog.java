package com.politest.figure.application.port.out;

import com.politest.figure.domain.model.DatasetInfo;
import com.politest.figure.domain.model.FigureWithPositions;
import java.util.List;
import java.util.Optional;

/** Outbound port: where the application obtains figure data from. */
public interface FigureCatalog {

    /** Active figures with texts in the given language ({@code es}, {@code ca} or {@code en}). */
    List<FigureWithPositions> findAllActive(String lang);

    Optional<FigureWithPositions> findByCode(String code, String lang);

    DatasetInfo datasetInfo();
}
