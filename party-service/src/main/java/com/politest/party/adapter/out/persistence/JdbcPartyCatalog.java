package com.politest.party.adapter.out.persistence;

import com.politest.party.application.port.out.PartyCatalog;
import com.politest.party.domain.model.AxisCode;
import com.politest.party.domain.model.Confidence;
import com.politest.party.domain.model.DatasetInfo;
import com.politest.party.domain.model.Party;
import com.politest.party.domain.model.PartyCode;
import com.politest.party.domain.model.Percentage;
import com.politest.party.domain.model.Position;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Outbound adapter: reads the party catalogue from MySQL with {@link JdbcClient}. */
@Repository
class JdbcPartyCatalog implements PartyCatalog {

    private final JdbcClient jdbc;

    JdbcPartyCatalog(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Party> findAllActive() {
        record PartyRow(String code, String name, String color) {
        }
        List<PartyRow> rows = jdbc.sql("SELECT code, name, color FROM party WHERE active = 1 ORDER BY sort_order")
                .query((rs, n) -> new PartyRow(rs.getString("code"), rs.getString("name"), rs.getString("color")))
                .list();

        record PositionRow(String partyCode, Position position) {
        }
        Map<String, List<Position>> byParty = jdbc.sql("""
                        SELECT pp.party_code, pp.axis_code, pp.right_pct, pp.confidence, pp.note, pp.source_url
                        FROM party_position pp JOIN party p ON p.code = pp.party_code
                        WHERE p.active = 1
                        """)
                .query((rs, n) -> new PositionRow(rs.getString("party_code"),
                        new Position(AxisCode.of(rs.getString("axis_code")),
                                Percentage.of(rs.getInt("right_pct")),
                                Confidence.ofLevel(rs.getInt("confidence")),
                                rs.getString("note"), rs.getString("source_url"))))
                .list().stream()
                .collect(Collectors.groupingBy(PositionRow::partyCode, LinkedHashMap::new,
                        Collectors.mapping(PositionRow::position, Collectors.toList())));

        return rows.stream()
                .map(row -> new Party(PartyCode.of(row.code()), row.name(), row.color(),
                        byParty.getOrDefault(row.code(), List.of())))
                .toList();
    }

    @Override
    public Optional<Party> findByCode(PartyCode code) {
        return findAllActive().stream()
                .filter(party -> party.code().equals(code))
                .findFirst();
    }

    @Override
    public DatasetInfo datasetInfo() {
        return jdbc.sql("SELECT version, status, note, updated_on FROM dataset_info WHERE id = 1")
                .query((rs, n) -> new DatasetInfo(rs.getString("version"), rs.getString("status"),
                        rs.getString("note"), rs.getDate("updated_on").toLocalDate()))
                .single();
    }
}
