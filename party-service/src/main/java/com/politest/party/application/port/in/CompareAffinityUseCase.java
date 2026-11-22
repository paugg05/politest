package com.politest.party.application.port.in;

import com.politest.party.domain.model.DatasetInfo;
import java.util.List;
import java.util.Map;

/** Inbound port: compare a person's positions with every active party. */
public interface CompareAffinityUseCase {

    Result compare(Query query);

    /**
     * @param scores  person's percentages by axis (0 to 100)
     * @param weights optional importance by axis (0 to 10)
     */
    record Query(Map<String, Integer> scores, Map<String, Double> weights) {
    }

    record AxisGap(String axis, int user, int party, int confidence, String note, String sourceUrl) {
    }

    record PartyMatch(String code, String name, String color, int affinity, List<AxisGap> axes) {
    }

    record Result(DatasetInfo dataset, List<PartyMatch> matches) {
    }
}
