package com.politest.party.application.port.in;

import com.politest.party.domain.model.DatasetInfo;
import com.politest.party.domain.model.Party;
import java.util.List;
import java.util.Optional;

/** Inbound port: read the catalogue of parties. */
public interface QueryPartiesUseCase {

    Catalog listParties();

    /** Finds a party by code, ignoring case. An invalid code simply finds nothing. */
    Optional<Party> findParty(String code);

    record Catalog(DatasetInfo dataset, List<Party> parties) {
    }
}
