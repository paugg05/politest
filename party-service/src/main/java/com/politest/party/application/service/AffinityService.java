package com.politest.party.application.service;

import com.politest.party.application.InvalidAffinityRequestException;
import com.politest.party.application.port.in.CompareAffinityUseCase;
import com.politest.party.application.port.in.QueryPartiesUseCase;
import com.politest.party.application.port.out.PartyCatalog;
import com.politest.party.domain.AffinityCalculator;
import com.politest.party.domain.model.Axes;
import com.politest.party.domain.model.PartyWithPositions;
import com.politest.party.domain.model.Position;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Implements the use cases of the service. It only depends on ports and on the domain. */
public class AffinityService implements CompareAffinityUseCase, QueryPartiesUseCase {

    private final PartyCatalog catalog;

    public AffinityService(PartyCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public Result compare(Query query) {
        validate(query);
        Map<String, Integer> scores = query.scores();

        List<PartyMatch> matches = catalog.findAllActive().stream()
                .map(party -> toMatch(party, scores, query.weights()))
                .sorted(Comparator.comparingInt(PartyMatch::affinity).reversed())
                .toList();
        return new Result(catalog.datasetInfo(), matches);
    }

    @Override
    public Catalog listParties() {
        return new Catalog(catalog.datasetInfo(), catalog.findAllActive());
    }

    @Override
    public Optional<PartyWithPositions> findParty(String code) {
        return catalog.findByCode(code);
    }

    private static PartyMatch toMatch(PartyWithPositions party, Map<String, Integer> scores,
                                      Map<String, Double> weights) {
        double affinity = AffinityCalculator.affinity(scores, party.asMap(), weights);
        List<AxisGap> gaps = party.positions().stream()
                .filter(position -> scores.containsKey(position.axis()))
                .map(position -> toGap(position, scores.get(position.axis())))
                .toList();
        return new PartyMatch(party.party().code(), party.party().name(), party.party().color(),
                (int) Math.round(affinity), gaps);
    }

    private static AxisGap toGap(Position position, int user) {
        return new AxisGap(position.axis(), user, position.rightPct(), position.confidence(),
                position.note(), position.sourceUrl());
    }

    private static void validate(Query query) {
        if (query == null || query.scores() == null || query.scores().isEmpty()) {
            throw new InvalidAffinityRequestException("Falta «scores»: porcentajes por eje.");
        }
        for (Map.Entry<String, Integer> entry : query.scores().entrySet()) {
            if (!Axes.CODES.contains(entry.getKey())) {
                throw new InvalidAffinityRequestException("Eje desconocido: " + entry.getKey());
            }
            if (entry.getValue() == null || entry.getValue() < 0 || entry.getValue() > 100) {
                throw new InvalidAffinityRequestException(
                        "El porcentaje de " + entry.getKey() + " debe estar entre 0 y 100.");
            }
        }
        if (query.weights() != null) {
            for (Map.Entry<String, Double> entry : query.weights().entrySet()) {
                if (!Axes.CODES.contains(entry.getKey())) {
                    throw new InvalidAffinityRequestException("Eje desconocido en «weights»: " + entry.getKey());
                }
                if (entry.getValue() == null || entry.getValue() < 0 || entry.getValue() > 10) {
                    throw new InvalidAffinityRequestException(
                            "El peso de " + entry.getKey() + " debe estar entre 0 y 10.");
                }
            }
        }
    }
}
