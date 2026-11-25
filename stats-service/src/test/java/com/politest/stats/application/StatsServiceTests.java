package com.politest.stats.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.politest.stats.application.port.in.RegisterCompletedTestUseCase.Command;
import com.politest.stats.application.port.in.RegisterCompletedTestUseCase.Outcome;
import com.politest.stats.application.service.StatsService;
import com.politest.stats.domain.error.DomainValidationException;
import com.politest.stats.domain.model.AxisCode;
import com.politest.stats.domain.model.Language;
import com.politest.stats.domain.model.StatsSummary;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatsServiceTests {

    private static final String AT = "2026-11-25T14:30:00Z";

    private InMemoryCompletedTestStore store;
    private StatsService service;

    @BeforeEach
    void setUp() {
        store = new InMemoryCompletedTestStore();
        service = new StatsService(store);
    }

    private static Command command(String id, String language, int econ) {
        return new Command(id, AT, language, Map.of("ECON", econ));
    }

    @Test
    void registersANewTest() {
        Outcome outcome = service.register(command("00000000-0000-0000-0000-000000000001", "es", 40));

        assertEquals(Outcome.REGISTERED, outcome);
        assertEquals(1, store.size());
    }

    @Test
    void processingTheSameEventTwiceIsIdempotent() {
        Command event = command("00000000-0000-0000-0000-000000000001", "es", 40);

        assertEquals(Outcome.REGISTERED, service.register(event));
        assertEquals(Outcome.DUPLICATE, service.register(event));
        assertEquals(1L, service.summary().totalTests());
    }

    @Test
    void invalidEventsAreRejectedAndNotStored() {
        assertThrows(DomainValidationException.class,
                () -> service.register(command("not-a-uuid", "es", 40)));
        assertEquals(0, store.size());
    }

    @Test
    void summaryCountsPerLanguageAndAveragesPerAxis() {
        service.register(command("00000000-0000-0000-0000-000000000001", "es", 20));
        service.register(command("00000000-0000-0000-0000-000000000002", "es", 60));
        service.register(command("00000000-0000-0000-0000-000000000003", "ca", 70));

        StatsSummary summary = service.summary();

        assertEquals(3L, summary.totalTests());
        assertEquals(2L, summary.testsByLanguage().get(Language.ES));
        assertEquals(1L, summary.testsByLanguage().get(Language.CA));
        assertEquals(50.0, summary.averageRightPct().get(AxisCode.ECON), 0.001);
    }

    @Test
    void emptySummaryHasNoFigures() {
        StatsSummary summary = service.summary();

        assertEquals(0L, summary.totalTests());
        assertEquals(Map.of(), summary.averageRightPct());
    }
}
