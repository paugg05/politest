package com.politest.figure.affinity;

import java.util.Map;

/**
 * Cálculo de afinidad, sin dependencias de Spring (fácil de probar).
 *
 * <p>Para cada eje se mide la distancia entre la posición de la persona y la del partido (0-100) y se
 * hace la media ponderada por los pesos de cada eje. La afinidad es 100 menos esa distancia media:
 * 100 % significa posiciones idénticas en todos los ejes comparados.
 */
public final class AffinityCalculator {

    private AffinityCalculator() {
    }

    /**
     * @param user    porcentajes de la persona por eje (0-100)
     * @param party   posiciones del partido por eje (0-100)
     * @param weights importancia de cada eje (por defecto 1); puede ser nulo o incompleto
     * @return afinidad de 0 a 100, o 0 si no hay ningún eje en común
     */
    public static double affinity(Map<String, Integer> user, Map<String, Integer> party,
                                  Map<String, Double> weights) {
        double weightedDistance = 0;
        double totalWeight = 0;
        for (Map.Entry<String, Integer> entry : user.entrySet()) {
            Integer partyValue = party.get(entry.getKey());
            if (partyValue == null) {
                continue;
            }
            double weight = weights == null ? 1.0 : weights.getOrDefault(entry.getKey(), 1.0);
            weightedDistance += weight * Math.abs(entry.getValue() - partyValue);
            totalWeight += weight;
        }
        if (totalWeight == 0) {
            return 0;
        }
        return 100.0 - weightedDistance / totalWeight;
    }
}
