package com.politest.party.application.port.in;

import com.politest.party.domain.model.DatasetInfo;
import com.politest.party.domain.model.PartyWithPositions;
import java.util.List;
import java.util.Optional;

/** Inbound port: read the catalogue of parties. */
public interface QueryPartiesUseCase {

    Catalog listParties();

    Optional<PartyWithPositions> findParty(String code);

    record Catalog(DatasetInfo dataset, List<PartyWithPositions> parties) {
    }
}
