package com.politest.party.application.service;

import com.politest.party.application.port.in.CompareAffinityUseCase;
import com.politest.party.application.port.in.QueryPartiesUseCase;
import com.politest.party.application.port.out.PartyCatalog;
import com.politest.party.domain.AffinityCalculator;
import com.politest.party.domain.error.DomainValidationException;
import com.politest.party.domain.model.AxisScores;
import com.politest.party.domain.model.AxisWeights;
import com.politest.party.domain.model.Party;
import com.politest.party.domain.model.PartyCode;
import com.politest.party.domain.model.Position;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Implements the use cases of the service. It translates raw input into domain objects (which validate themselves),
 * orchestrates the domain and talks to the outside only through ports.
 */
public class AffinityService implements CompareAffinityUseCase, QueryPartiesUseCase {

    private final PartyCatalog catalog;

    public AffinityService(PartyCatalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public Result compare(Query query) {
        if (query == null) {
            throw new DomainValidationException("Falta «scores»: porcentajes por eje.");
        }
        AxisScores scores = AxisScores.of(query.scores());
        AxisWeights weights = AxisWeights.of(query.weights());

        List<PartyMatch> matches = catalog.findAllActive().stream()
                .map(party -> toMatch(party, scores, weights))
                .sorted(Comparator.comparingInt(PartyMatch::affinity).reversed())
                .toList();
        return new Result(catalog.datasetInfo(), matches);
    }

    @Override
    public Catalog listParties() {
        return new Catalog(catalog.datasetInfo(), catalog.findAllActive());
    }

    @Override
    public Optional<Party> findParty(String code) {
        try {
            return catalog.findByCode(PartyCode.parse(code));
        } catch (DomainValidationException e) {
            return Optional.empty();
        }
    }

    private static PartyMatch toMatch(Party party, AxisScores scores, AxisWeights weights) {
        int affinity = AffinityCalculator.affinity(scores, party, weights).rounded();
        List<AxisGap> gaps = party.positions().stream()
                .filter(position -> scores.on(position.axis()).isPresent())
                .map(position -> toGap(position, scores))
                .toList();
        return new PartyMatch(party.code().value(), party.name(), party.color(), affinity, gaps);
    }

    private static AxisGap toGap(Position position, AxisScores scores) {
        int user = scores.on(position.axis()).orElseThrow().value();
        return new AxisGap(position.axis().name(), user, position.rightPct().value(),
                position.confidence().level(), position.note(), position.sourceUrl());
    }
}
