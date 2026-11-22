package com.politest.politest.scoring;

import com.politest.politest.catalog.CatalogService.QuestionScoring;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cálculo puro (sin Spring ni base de datos), fácil de probar.
 *
 * Respuesta de -2 a +2 (null = omitida). Para cada eje:
 *   bruto  = suma(respuesta * dirección * peso)
 *   máximo = suma(2 * peso), solo de las preguntas respondidas
 *   porcentaje hacia el polo derecho = (bruto / máximo + 1) / 2 * 100
 * Un eje sin respuestas queda en 50 %.
 */
final class ScoreCalculator {

    private ScoreCalculator() {
    }

    static Map<String, Integer> rightPercentages(Map<Long, Integer> answers,
                                                 Map<Long, QuestionScoring> info,
                                                 List<String> axisOrder) {
        Map<String, double[]> acc = new HashMap<>(); // [bruto, máximo]
        for (Map.Entry<Long, Integer> entry : answers.entrySet()) {
            Integer value = entry.getValue();
            QuestionScoring q = info.get(entry.getKey());
            if (value == null || q == null) {
                continue;
            }
            double[] v = acc.computeIfAbsent(q.axisCode(), k -> new double[2]);
            v[0] += value * q.direction() * q.weight();
            v[1] += 2 * q.weight();
        }

        Map<String, Integer> result = new LinkedHashMap<>();
        for (String axis : axisOrder) {
            double[] v = acc.get(axis);
            int pct = (v == null || v[1] == 0) ? 50 : (int) Math.round((v[0] / v[1] + 1) * 50);
            result.put(axis, Math.max(0, Math.min(100, pct)));
        }
        return result;
    }
}
