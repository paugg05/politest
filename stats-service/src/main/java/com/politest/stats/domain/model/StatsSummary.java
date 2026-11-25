package com.politest.stats.domain.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Aggregated figures over every completed test received so far. */
public record StatsSummary(long totalTests, Map<Language, Long> testsByLanguage, Map<AxisCode, Double> averageRightPct) {

    public StatsSummary {
        testsByLanguage = Collections.unmodifiableMap(new EnumMap<>(copyOrEmpty(testsByLanguage, Language.class)));
        averageRightPct = Collections.unmodifiableMap(new EnumMap<>(copyOrEmpty(averageRightPct, AxisCode.class)));
    }

    public static StatsSummary empty() {
        return new StatsSummary(0, Map.of(), Map.of());
    }

    private static <K extends Enum<K>, V> Map<K, V> copyOrEmpty(Map<K, V> source, Class<K> type) {
        Map<K, V> copy = new EnumMap<>(type);
        if (source != null) {
            copy.putAll(source);
        }
        return copy;
    }
}
