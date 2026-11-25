package com.politest.stats.adapter.out.persistence;

import com.politest.stats.application.port.out.CompletedTestStore;
import com.politest.stats.domain.model.AxisCode;
import com.politest.stats.domain.model.CompletedTest;
import com.politest.stats.domain.model.Language;
import com.politest.stats.domain.model.StatsSummary;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Outbound adapter: stores completed tests in MySQL with {@link JdbcClient}. */
@Repository
class JdbcCompletedTestStore implements CompletedTestStore {

    private final JdbcClient jdbc;

    JdbcCompletedTestStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Idempotent: the primary key on the event id rejects a second insert of the same event. */
    @Override
    @Transactional
    public boolean saveIfAbsent(CompletedTest test) {
        String id = test.id().toString();
        try {
            jdbc.sql("INSERT INTO completed_test (event_id, occurred_at, language) VALUES (:id, :at, :language)")
                    .param("id", id)
                    .param("at", LocalDateTime.ofInstant(test.occurredAt(), ZoneOffset.UTC))
                    .param("language", test.language().code())
                    .update();
        } catch (DuplicateKeyException e) {
            return false;
        }
        test.scores().forEach((axis, percentage) ->
                jdbc.sql("INSERT INTO completed_test_score (event_id, axis, right_pct) VALUES (:id, :axis, :pct)")
                        .param("id", id)
                        .param("axis", axis.name())
                        .param("pct", percentage.value())
                        .update());
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public StatsSummary summary() {
        long total = jdbc.sql("SELECT COUNT(*) FROM completed_test").query(Long.class).single();

        record LanguageRow(String language, long total) {
        }
        Map<Language, Long> byLanguage = new EnumMap<>(Language.class);
        jdbc.sql("SELECT language, COUNT(*) AS total FROM completed_test GROUP BY language")
                .query((rs, n) -> new LanguageRow(rs.getString("language"), rs.getLong("total")))
                .list()
                .forEach(row -> byLanguage.put(Language.of(row.language()), row.total()));

        record AxisRow(String axis, double average) {
        }
        Map<AxisCode, Double> averages = new EnumMap<>(AxisCode.class);
        jdbc.sql("SELECT axis, AVG(right_pct) AS average FROM completed_test_score GROUP BY axis")
                .query((rs, n) -> new AxisRow(rs.getString("axis"), rs.getDouble("average")))
                .list()
                .forEach(row -> averages.put(AxisCode.of(row.axis()), row.average()));

        return new StatsSummary(total, byLanguage, averages);
    }
}
