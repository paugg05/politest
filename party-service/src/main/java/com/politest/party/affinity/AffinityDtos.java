package com.politest.party.affinity;

import com.politest.party.party.PartyModels.DatasetInfo;
import java.util.List;
import java.util.Map;

public final class AffinityDtos {

    private AffinityDtos() {
    }

    public record AffinityRequest(Map<String, Integer> scores, Map<String, Double> weights) {
    }

    public record AxisGap(String axis, int user, int party, int confidence, String note, String sourceUrl) {
    }

    public record PartyMatch(String code, String name, String color, int affinity, List<AxisGap> axes) {
    }

    public record AffinityResponse(DatasetInfo dataset, List<PartyMatch> matches) {
    }
}
