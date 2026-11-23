package com.politest.figure.domain;

import com.politest.figure.domain.model.Affinity;
import com.politest.figure.domain.model.AxisCode;
import com.politest.figure.domain.model.AxisScores;
import com.politest.figure.domain.model.AxisWeights;
import com.politest.figure.domain.model.Figure;
import com.politest.figure.domain.model.Position;

/**
 * Domain service: computes how close a person is to a historical figure.
 *
 * <p>For every axis the distance between the person's position and the figure's position (0 to 100) is measured,
 * and the weighted mean of those distances is taken. Affinity is 100 minus that mean distance.
 * Axes on which the figure has no position (because they were not an issue in their time) are ignored;
 * with no axis in common the affinity is 0.
 */
public final class AffinityCalculator {

    private AffinityCalculator() {
    }

    public static Affinity affinity(AxisScores person, Figure figure, AxisWeights weights) {
        double weightedDistance = 0;
        double totalWeight = 0;
        for (AxisCode axis : person.axes()) {
            Position position = figure.positionOn(axis).orElse(null);
            if (position == null) {
                continue;
            }
            double weight = weights.weightOf(axis);
            int distance = person.on(axis).orElseThrow().distanceTo(position.rightPct());
            weightedDistance += weight * distance;
            totalWeight += weight;
        }
        if (totalWeight == 0) {
            return new Affinity(0);
        }
        return new Affinity(100.0 - weightedDistance / totalWeight);
    }
}
