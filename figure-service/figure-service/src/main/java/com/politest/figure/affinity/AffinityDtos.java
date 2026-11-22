package com.politest.figure.affinity;

import com.politest.figure.figure.FigureModels.DatasetInfo;
import java.util.List;
import java.util.Map;

public final class AffinityDtos {

    private AffinityDtos() {
    }

    public record AffinityRequest(Map<String, Integer> scores, Map<String, Double> weights) {
    }

    public record AxisGap(String axis, int user, int figure, int confidence, String note) {
    }

    public record FigureMatch(String code, String name, String description, int birthYear, int deathYear,
                              String color, int affinity, List<AxisGap> axes) {
    }

    public record AffinityResponse(DatasetInfo dataset, List<FigureMatch> matches) {
    }
}
