package com.politest.stats.adapter.in.web;

import com.politest.stats.application.port.in.QueryStatsUseCase;
import com.politest.stats.domain.model.AxisCode;
import com.politest.stats.domain.model.Language;
import com.politest.stats.domain.model.StatsSummary;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Inbound adapter: exposes the statistics over REST. It only talks to the inbound port. */
@RestController
@RequestMapping("/api/v1")
class StatsController {

    private final QueryStatsUseCase queryStats;

    StatsController(QueryStatsUseCase queryStats) {
        this.queryStats = queryStats;
    }

    @GetMapping("/stats")
    StatsResponse stats() {
        return StatsResponse.from(queryStats.summary());
    }

    /** Public JSON model, decoupled from the domain. Averages are rounded to one decimal. */
    record StatsResponse(long totalTests, Map<String, Long> testsByLanguage, Map<String, Double> averageRightPct) {

        static StatsResponse from(StatsSummary summary) {
            Map<String, Long> byLanguage = new LinkedHashMap<>();
            for (Language language : Language.values()) {
                byLanguage.put(language.code(), summary.testsByLanguage().getOrDefault(language, 0L));
            }
            Map<String, Double> averages = new LinkedHashMap<>();
            for (AxisCode axis : AxisCode.values()) {
                Double average = summary.averageRightPct().get(axis);
                if (average != null) {
                    averages.put(axis.name(), Math.round(average * 10.0) / 10.0);
                }
            }
            return new StatsResponse(summary.totalTests(), byLanguage, averages);
        }
    }
}
