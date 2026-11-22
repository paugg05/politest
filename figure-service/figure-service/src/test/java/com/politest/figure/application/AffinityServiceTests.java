package com.politest.figure.application;

import static com.politest.figure.application.InMemoryFigureCatalog.figure;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.politest.figure.application.port.in.CompareAffinityUseCase.FigureMatch;
import com.politest.figure.application.port.in.CompareAffinityUseCase.Query;
import com.politest.figure.application.port.in.CompareAffinityUseCase.Result;
import com.politest.figure.application.service.AffinityService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Exercises the use cases against an in-memory catalogue: the application layer is tested without Spring or a database. */
class AffinityServiceTests {

    private final AffinityService service = new AffinityService(new InMemoryFigureCatalog(
            figure("LEFT", 10, 10),
            figure("CENTRE", 50, 50),
            figure("RIGHT", 90, 90)));

    @Test
    void matchesAreSortedByDescendingAffinity() {
        Result result = service.compare(new Query(Map.of("ECON", 85, "WELFARE", 85), null), "es");

        List<String> order = result.matches().stream().map(FigureMatch::code).toList();
        assertEquals(List.of("RIGHT", "CENTRE", "LEFT"), order);
    }

    @Test
    void identicalPositionsGiveFullAffinity() {
        Result result = service.compare(new Query(Map.of("ECON", 50, "WELFARE", 50), null), "es");

        assertEquals("CENTRE", result.matches().get(0).code());
        assertEquals(100, result.matches().get(0).affinity());
    }

    @Test
    void gapsOnlyIncludeTheAxesTheUserAnswered() {
        Result result = service.compare(new Query(Map.of("ECON", 40), null), "es");

        assertEquals(1, result.matches().get(0).axes().size());
        assertEquals("ECON", result.matches().get(0).axes().get(0).axis());
    }

    @Test
    void weightsAreAppliedToTheComparison() {
        Result unweighted = service.compare(new Query(Map.of("ECON", 90, "WELFARE", 10), null), "es");
        Result weighted = service.compare(new Query(Map.of("ECON", 90, "WELFARE", 10), Map.of("ECON", 10.0)), "es");

        // without weights the three figures are equally distant (60 each); weighting ECON favours RIGHT
        assertTrue(unweighted.matches().stream().allMatch(match -> match.affinity() == 60));
        assertEquals("RIGHT", weighted.matches().get(0).code());
    }

    @Test
    void textsComeInTheRequestedLanguage() {
        Result result = service.compare(new Query(Map.of("ECON", 50), null), "ca");

        assertTrue(result.matches().get(0).name().endsWith("[ca]"));
    }

    @Test
    void rejectsUnsupportedLanguages() {
        InvalidAffinityRequestException e = assertThrows(InvalidAffinityRequestException.class,
                () -> service.compare(new Query(Map.of("ECON", 50), null), "fr"));
        assertTrue(e.getMessage().contains("fr"));
    }

    @Test
    void rejectsMissingScores() {
        assertThrows(InvalidAffinityRequestException.class, () -> service.compare(new Query(null, null), "es"));
        assertThrows(InvalidAffinityRequestException.class, () -> service.compare(new Query(Map.of(), null), "es"));
        assertThrows(InvalidAffinityRequestException.class, () -> service.compare(null, "es"));
    }

    @Test
    void rejectsUnknownAxes() {
        InvalidAffinityRequestException e = assertThrows(InvalidAffinityRequestException.class,
                () -> service.compare(new Query(Map.of("MAGIC", 10), null), "es"));
        assertTrue(e.getMessage().contains("MAGIC"));
    }

    @Test
    void rejectsScoresOutOfRange() {
        assertThrows(InvalidAffinityRequestException.class,
                () -> service.compare(new Query(Map.of("ECON", 101), null), "es"));
        assertThrows(InvalidAffinityRequestException.class,
                () -> service.compare(new Query(Map.of("ECON", -1), null), "es"));
    }

    @Test
    void rejectsInvalidWeights() {
        Map<String, Integer> scores = Map.of("ECON", 50);
        assertThrows(InvalidAffinityRequestException.class,
                () -> service.compare(new Query(scores, Map.of("ECON", 11.0)), "es"));
        assertThrows(InvalidAffinityRequestException.class,
                () -> service.compare(new Query(scores, Map.of("MAGIC", 1.0)), "es"));
    }

    @Test
    void findsAFigureByCodeIgnoringCase() {
        assertTrue(service.findFigure("centre", "es").isPresent());
        assertTrue(service.findFigure("NOPE", "es").isEmpty());
    }

    @Test
    void listsAllFiguresWithTheirDataset() {
        assertEquals(3, service.listFigures("es").figures().size());
        assertEquals("PROVISIONAL", service.listFigures("es").dataset().status());
    }
}
