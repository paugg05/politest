package com.politest.party.application;

import com.politest.party.application.port.out.PartyCatalog;
import com.politest.party.domain.model.DatasetInfo;
import com.politest.party.domain.model.Party;
import com.politest.party.domain.model.PartyCode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Test double for the {@link PartyCatalog} port: no database needed. */
class InMemoryPartyCatalog implements PartyCatalog {

    private final List<Party> parties;

    InMemoryPartyCatalog(Party... parties) {
        this.parties = List.of(parties);
    }

    @Override
    public List<Party> findAllActive() {
        return parties;
    }

    @Override
    public Optional<Party> findByCode(PartyCode code) {
        return parties.stream().filter(party -> party.code().equals(code)).findFirst();
    }

    @Override
    public DatasetInfo datasetInfo() {
        return new DatasetInfo("test", "PROVISIONAL", "test data", LocalDate.of(2026, 1, 1));
    }
}
