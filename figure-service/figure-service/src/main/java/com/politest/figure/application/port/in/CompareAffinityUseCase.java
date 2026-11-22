package com.politest.figure.application.port.in;

import com.politest.figure.domain.model.DatasetInfo;
import java.util.List;
import java.util.Map;

/** Inbound port: compare a person's positions with every active historical figure. */
public interface CompareAffinityUseCase {

    Result compare(Query query, String lang);

    /**
     * @param scores  person's percentages by axis (0 to 100)
     * @param weights optional importance by axis (0 to 10)
     */
    record Query(Map<String, Integer> scores, Map<String, Double> weights) {
    }

    record AxisGap(String axis, int user, int figure, int confidence, String note) {
    }

    record FigureMatch(String code, String name, String description, int birthYear, int deathYear,
                       String color, int affinity, List<AxisGap> axes) {
    }

    record Result(DatasetInfo dataset, List<FigureMatch> matches) {
    }
}
