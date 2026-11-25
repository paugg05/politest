package com.politest.stats.application.port.in;

import com.politest.stats.domain.model.StatsSummary;

/** Inbound port: read the aggregated statistics. */
public interface QueryStatsUseCase {

    StatsSummary summary();
}
