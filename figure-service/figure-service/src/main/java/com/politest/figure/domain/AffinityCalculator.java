package com.politest.figure.domain;

import java.util.Map;

/**
 * Computes how close a person is to a historical figure.
 *
 * <p>For every axis the distance between the person's position and the figure's position (0 to 100) is measured,
 * and the weighted mean of those distances is taken. Affinity is 100 minus that mean distance:
 * 100 means identical positions on every compared axis.
 */
public final class AffinityCalculator {

    private AffinityCalculator() {
    }

    /**
     * @param user    person's percentages by axis (0 to 100)
     * @param figure  figure's positions by axis (0 to 100)
     * @param weights importance of each axis (1 by default); may be null or incomplete
     * @return affinity from 0 to 100, or 0 when there is no axis in common
     */
    public static double affinity(Map<String, Integer> user, Map<String, Integer> figure,
                                  Map<String, Double> weights) {
        double weightedDistance = 0;
        double totalWeight = 0;
        for (Map.Entry<String, Integer> entry : user.entrySet()) {
            Integer figureValue = figure.get(entry.getKey());
            if (figureValue == null) {
                continue;
            }
            double weight = weights == null ? 1.0 : weights.getOrDefault(entry.getKey(), 1.0);
            weightedDistance += weight * Math.abs(entry.getValue() - figureValue);
            totalWeight += weight;
        }
        if (totalWeight == 0) {
            return 0;
        }
        return 100.0 - weightedDistance / totalWeight;
    }
}
