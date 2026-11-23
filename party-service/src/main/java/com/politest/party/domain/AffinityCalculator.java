package com.politest.party.domain;

import com.politest.party.domain.model.Affinity;
import com.politest.party.domain.model.AxisCode;
import com.politest.party.domain.model.AxisScores;
import com.politest.party.domain.model.AxisWeights;
import com.politest.party.domain.model.Party;
import com.politest.party.domain.model.Position;

/**
 * Domain service: computes how close a person is to a party.
 *
 * <p>For every axis the distance between the person's position and the party's position (0 to 100) is measured,
 * and the weighted mean of those distances is taken. Affinity is 100 minus that mean distance.
 * Axes on which the party has no position are ignored; with no axis in common the affinity is 0.
 */
public final class AffinityCalculator {

    private AffinityCalculator() {
    }

    public static Affinity affinity(AxisScores person, Party party, AxisWeights weights) {
        double weightedDistance = 0;
        double totalWeight = 0;
        for (AxisCode axis : person.axes()) {
            Position position = party.positionOn(axis).orElse(null);
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
