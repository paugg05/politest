package com.politest.stats.application;

import com.politest.stats.application.port.out.CompletedTestStore;
import com.politest.stats.domain.model.AxisCode;
import com.politest.stats.domain.model.CompletedTest;
import com.politest.stats.domain.model.EventId;
import com.politest.stats.domain.model.Language;
import com.politest.stats.domain.model.StatsSummary;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Test double for the outbound port: lets the application service be tested without a database. */
public class InMemoryCompletedTestStore implements CompletedTestStore {

    private final Map<EventId, CompletedTest> tests = new LinkedHashMap<>();

    @Override
    public boolean saveIfAbsent(CompletedTest test) {
        return tests.putIfAbsent(test.id(), test) == null;
    }

    @Override
    public StatsSummary summary() {
        Map<Language, Long> byLanguage = new EnumMap<>(Language.class);
        Map<AxisCode, double[]> sums = new EnumMap<>(AxisCode.class);
        for (CompletedTest test : tests.values()) {
            byLanguage.merge(test.language(), 1L, Long::sum);
            test.scores().forEach((axis, pct) -> {
                double[] acc = sums.computeIfAbsent(axis, a -> new double[2]);
                acc[0] += pct.value();
                acc[1]++;
            });
        }
        Map<AxisCode, Double> averages = new EnumMap<>(AxisCode.class);
        sums.forEach((axis, acc) -> averages.put(axis, acc[0] / acc[1]));
        return new StatsSummary(tests.size(), byLanguage, averages);
    }

    public int size() {
        return tests.size();
    }
}
