package com.politest.stats.application.port.out;

import com.politest.stats.domain.model.CompletedTest;
import com.politest.stats.domain.model.StatsSummary;

/** Outbound port: persistence of completed tests and the figures computed from them. */
public interface CompletedTestStore {

    /**
     * Stores the test unless one with the same id already exists.
     *
     * @return true if it was stored, false if it was a duplicate
     */
    boolean saveIfAbsent(CompletedTest test);

    StatsSummary summary();
}
