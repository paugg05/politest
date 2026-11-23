package com.politest.party.application;

import static com.politest.party.domain.TestParties.party;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.politest.party.application.port.in.CompareAffinityUseCase.PartyMatch;
import com.politest.party.application.port.in.CompareAffinityUseCase.Query;
import com.politest.party.application.port.in.CompareAffinityUseCase.Result;
import com.politest.party.application.service.AffinityService;
import com.politest.party.domain.error.DomainValidationException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Exercises the use cases against an in-memory catalogue: the application layer is tested without Spring or a database. */
class AffinityServiceTests {

    private final AffinityService service = new AffinityService(new InMemoryPartyCatalog(
            party("LEFT", 10, 10),
            party("CENTRE", 50, 50),
            party("RIGHT", 90, 90)));

    @Test
    void matchesAreSortedByDescendingAffinity() {
        Result result = service.compare(new Query(Map.of("ECON", 85, "WELFARE", 85), null));

        List<String> order = result.matches().stream().map(PartyMatch::code).toList();
        assertEquals(List.of("RIGHT", "CENTRE", "LEFT"), order);
    }

    @Test
    void identicalPositionsGiveFullAffinity() {
        Result result = service.compare(new Query(Map.of("ECON", 50, "WELFARE", 50), null));

        assertEquals("CENTRE", result.matches().get(0).code());
        assertEquals(100, result.matches().get(0).affinity());
    }

    @Test
    void gapsOnlyIncludeTheAxesTheUserAnswered() {
        Result result = service.compare(new Query(Map.of("ECON", 40), null));

        assertEquals(1, result.matches().get(0).axes().size());
        assertEquals("ECON", result.matches().get(0).axes().get(0).axis());
        assertEquals(40, result.matches().get(0).axes().get(0).user());
    }

    @Test
    void weightsAreAppliedToTheComparison() {
        Result unweighted = service.compare(new Query(Map.of("ECON", 90, "WELFARE", 10), null));
        Result weighted = service.compare(new Query(Map.of("ECON", 90, "WELFARE", 10), Map.of("ECON", 10.0)));

        // without weights the three parties are equally distant (60 each); weighting ECON favours RIGHT
        assertTrue(unweighted.matches().stream().allMatch(match -> match.affinity() == 60));
        assertEquals("RIGHT", weighted.matches().get(0).code());
    }

    @Test
    void resultCarriesTheDatasetInformation() {
        Result result = service.compare(new Query(Map.of("ECON", 50), null));

        assertEquals("test", result.dataset().version());
    }

    @Test
    void rejectsMissingScores() {
        assertThrows(DomainValidationException.class, () -> service.compare(new Query(null, null)));
        assertThrows(DomainValidationException.class, () -> service.compare(new Query(Map.of(), null)));
        assertThrows(DomainValidationException.class, () -> service.compare(null));
    }

    @Test
    void rejectsUnknownAxesWithAHelpfulMessage() {
        DomainValidationException e = assertThrows(DomainValidationException.class,
                () -> service.compare(new Query(Map.of("MAGIC", 10), null)));
        assertTrue(e.getMessage().contains("MAGIC"));
    }

    @Test
    void rejectsScoresOutOfRange() {
        assertThrows(DomainValidationException.class, () -> service.compare(new Query(Map.of("ECON", 101), null)));
        assertThrows(DomainValidationException.class, () -> service.compare(new Query(Map.of("ECON", -1), null)));
    }

    @Test
    void rejectsInvalidWeights() {
        Map<String, Integer> scores = Map.of("ECON", 50);
        assertThrows(DomainValidationException.class, () -> service.compare(new Query(scores, Map.of("ECON", 11.0))));
        assertThrows(DomainValidationException.class, () -> service.compare(new Query(scores, Map.of("MAGIC", 1.0))));
    }

    @Test
    void findsAPartyByCodeIgnoringCase() {
        assertTrue(service.findParty("centre").isPresent());
        assertTrue(service.findParty("NOPE").isEmpty());
    }

    @Test
    void anInvalidCodeFindsNothingInsteadOfFailing() {
        assertTrue(service.findParty("not a code!").isEmpty());
        assertTrue(service.findParty(null).isEmpty());
    }

    @Test
    void listsAllPartiesWithTheirDataset() {
        assertEquals(3, service.listParties().parties().size());
        assertEquals("PROVISIONAL", service.listParties().dataset().status());
    }
}
