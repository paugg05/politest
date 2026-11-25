package com.politest.stats.application.service;

import com.politest.stats.application.port.in.QueryStatsUseCase;
import com.politest.stats.application.port.in.RegisterCompletedTestUseCase;
import com.politest.stats.application.port.out.CompletedTestStore;
import com.politest.stats.domain.model.CompletedTest;
import com.politest.stats.domain.model.StatsSummary;

/** Application service: orchestrates the use cases. It has no framework annotations. */
public class StatsService implements RegisterCompletedTestUseCase, QueryStatsUseCase {

    private final CompletedTestStore store;

    public StatsService(CompletedTestStore store) {
        this.store = store;
    }

    @Override
    public Outcome register(Command command) {
        CompletedTest test = CompletedTest.of(
                command.eventId(), command.occurredAt(), command.language(), command.rightPct());
        return store.saveIfAbsent(test) ? Outcome.REGISTERED : Outcome.DUPLICATE;
    }

    @Override
    public StatsSummary summary() {
        return store.summary();
    }
}
