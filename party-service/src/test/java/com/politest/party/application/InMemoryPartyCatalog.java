package com.politest.party.application;

import com.politest.party.application.port.out.PartyCatalog;
import com.politest.party.domain.model.DatasetInfo;
import com.politest.party.domain.model.Party;
import com.politest.party.domain.model.PartyWithPositions;
import com.politest.party.domain.model.Position;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Test double for the {@link PartyCatalog} port: no database needed. */
class InMemoryPartyCatalog implements PartyCatalog {

    private final List<PartyWithPositions> parties;

    InMemoryPartyCatalog(PartyWithPositions... parties) {
        this.parties = List.of(parties);
    }

    static PartyWithPositions party(String code, int econ, int welfare) {
        return new PartyWithPositions(new Party(code, "Party " + code, "#336699"), List.of(
                new Position("ECON", econ, 2, "note", null),
                new Position("WELFARE", welfare, 3, "note", "https://example.org")));
    }

    @Override
    public List<PartyWithPositions> findAllActive() {
        return parties;
    }

    @Override
    public Optional<PartyWithPositions> findByCode(String code) {
        return parties.stream().filter(p -> p.party().code().equalsIgnoreCase(code)).findFirst();
    }

    @Override
    public DatasetInfo datasetInfo() {
        return new DatasetInfo("test", "PROVISIONAL", "test data", LocalDate.of(2026, 1, 1));
    }
}
