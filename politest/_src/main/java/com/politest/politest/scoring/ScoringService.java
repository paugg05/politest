package com.politest.politest.scoring;

import com.politest.politest.catalog.CatalogService;
import java.util.Map;
import org.springframework.stereotype.Service;

/** API pública del módulo "scoring". */
@Service
public class ScoringService {

    private final CatalogService catalog;

    public ScoringService(CatalogService catalog) {
        this.catalog = catalog;
    }

    /**
     * @param answers id de pregunta -> respuesta (-2..2), o null si se omitió
     * @return código de eje -> porcentaje hacia el polo derecho (0..100), en el orden de los ejes
     */
    public Map<String, Integer> rightPercentages(Map<Long, Integer> answers) {
        return ScoreCalculator.rightPercentages(
                answers,
                catalog.scoringInfo(answers.keySet()),
                catalog.axisCodes());
    }
}
