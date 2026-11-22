package com.politest.party.affinity;

import com.politest.party.affinity.AffinityDtos.AffinityRequest;
import com.politest.party.affinity.AffinityDtos.AffinityResponse;
import com.politest.party.affinity.AffinityDtos.AxisGap;
import com.politest.party.affinity.AffinityDtos.PartyMatch;
import com.politest.party.party.Axes;
import com.politest.party.party.PartyModels.PartyWithPositions;
import com.politest.party.party.PartyModels.Position;
import com.politest.party.party.PartyRepository;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AffinityService {

    private final PartyRepository repository;

    public AffinityService(PartyRepository repository) {
        this.repository = repository;
    }

    public AffinityResponse compare(AffinityRequest request) {
        validate(request);
        Map<String, Integer> scores = request.scores();

        List<PartyMatch> matches = repository.findAllActive().stream()
                .map(p -> toMatch(p, scores, request.weights()))
                .sorted(Comparator.comparingInt(PartyMatch::affinity).reversed())
                .toList();
        return new AffinityResponse(repository.datasetInfo(), matches);
    }

    private static PartyMatch toMatch(PartyWithPositions party, Map<String, Integer> scores,
                                      Map<String, Double> weights) {
        double affinity = AffinityCalculator.affinity(scores, party.asMap(), weights);
        List<AxisGap> gaps = party.positions().stream()
                .filter(p -> scores.containsKey(p.axis()))
                .map(p -> toGap(p, scores.get(p.axis())))
                .toList();
        return new PartyMatch(party.party().code(), party.party().name(), party.party().color(),
                (int) Math.round(affinity), gaps);
    }

    private static AxisGap toGap(Position position, int user) {
        return new AxisGap(position.axis(), user, position.rightPct(), position.confidence(),
                position.note(), position.sourceUrl());
    }

    private static void validate(AffinityRequest request) {
        if (request == null || request.scores() == null || request.scores().isEmpty()) {
            throw new IllegalArgumentException("Falta «scores»: porcentajes por eje.");
        }
        for (Map.Entry<String, Integer> entry : request.scores().entrySet()) {
            if (!Axes.CODES.contains(entry.getKey())) {
                throw new IllegalArgumentException("Eje desconocido: " + entry.getKey());
            }
            if (entry.getValue() == null || entry.getValue() < 0 || entry.getValue() > 100) {
                throw new IllegalArgumentException("El porcentaje de " + entry.getKey() + " debe estar entre 0 y 100.");
            }
        }
        if (request.weights() != null) {
            for (Map.Entry<String, Double> entry : new LinkedHashMap<>(request.weights()).entrySet()) {
                if (!Axes.CODES.contains(entry.getKey())) {
                    throw new IllegalArgumentException("Eje desconocido en «weights»: " + entry.getKey());
                }
                if (entry.getValue() == null || entry.getValue() < 0 || entry.getValue() > 10) {
                    throw new IllegalArgumentException("El peso de " + entry.getKey() + " debe estar entre 0 y 10.");
                }
            }
        }
    }
}
