package com.politest.party.application.port.out;

import com.politest.party.domain.model.DatasetInfo;
import com.politest.party.domain.model.PartyWithPositions;
import java.util.List;
import java.util.Optional;

/** Outbound port: where the application obtains party data from. */
public interface PartyCatalog {

    List<PartyWithPositions> findAllActive();

    Optional<PartyWithPositions> findByCode(String code);

    DatasetInfo datasetInfo();
}
