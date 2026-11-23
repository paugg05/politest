package com.politest.figure.application.port.out;

import com.politest.figure.domain.model.DatasetInfo;
import com.politest.figure.domain.model.Figure;
import com.politest.figure.domain.model.FigureCode;
import com.politest.figure.domain.model.Language;
import java.util.List;
import java.util.Optional;

/** Outbound port: where the application obtains figures from. Texts come in the requested language. */
public interface FigureCatalog {

    List<Figure> findAllActive(Language language);

    Optional<Figure> findByCode(FigureCode code, Language language);

    DatasetInfo datasetInfo();
}
