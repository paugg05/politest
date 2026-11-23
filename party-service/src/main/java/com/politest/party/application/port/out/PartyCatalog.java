package com.politest.party.application.port.out;

import com.politest.party.domain.model.DatasetInfo;
import com.politest.party.domain.model.Party;
import com.politest.party.domain.model.PartyCode;
import java.util.List;
import java.util.Optional;

/** Outbound port: where the application obtains parties from. */
public interface PartyCatalog {

    List<Party> findAllActive();

    Optional<Party> findByCode(PartyCode code);

    DatasetInfo datasetInfo();
}
