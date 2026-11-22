package com.politest.party.adapter.out.persistence;

import com.politest.party.application.port.out.PartyCatalog;
import com.politest.party.domain.model.Axes;
import com.politest.party.domain.model.DatasetInfo;
import com.politest.party.domain.model.Party;
import com.politest.party.domain.model.PartyWithPositions;
import com.politest.party.domain.model.Position;
import java.util.Comparator;
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
    public List<PartyWithPositions> findAllActive() {
        List<Party> parties = jdbc.sql("SELECT code, name, color FROM party WHERE active = 1 ORDER BY sort_order")
                .query((rs, n) -> new Party(rs.getString("code"), rs.getString("name"), rs.getString("color")))
                .list();

        record Row(String partyCode, Position position) {
        }
        Map<String, List<Position>> byParty = jdbc.sql("""
                        SELECT pp.party_code, pp.axis_code, pp.right_pct, pp.confidence, pp.note, pp.source_url
                        FROM party_position pp JOIN party p ON p.code = pp.party_code
                        WHERE p.active = 1
                        """)
                .query((rs, n) -> new Row(rs.getString("party_code"),
                        new Position(rs.getString("axis_code"), rs.getInt("right_pct"),
                                rs.getInt("confidence"), rs.getString("note"), rs.getString("source_url"))))
                .list().stream()
                .collect(Collectors.groupingBy(Row::partyCode, LinkedHashMap::new,
                        Collectors.mapping(Row::position, Collectors.toList())));

        return parties.stream()
                .map(party -> new PartyWithPositions(party, sorted(byParty.getOrDefault(party.code(), List.of()))))
                .toList();
    }

    @Override
    public Optional<PartyWithPositions> findByCode(String code) {
        return findAllActive().stream()
                .filter(party -> party.party().code().equalsIgnoreCase(code))
                .findFirst();
    }

    @Override
    public DatasetInfo datasetInfo() {
        return jdbc.sql("SELECT version, status, note, updated_on FROM dataset_info WHERE id = 1")
                .query((rs, n) -> new DatasetInfo(rs.getString("version"), rs.getString("status"),
                        rs.getString("note"), rs.getDate("updated_on").toLocalDate()))
                .single();
    }

    private static List<Position> sorted(List<Position> positions) {
        return positions.stream()
                .sorted(Comparator.comparingInt(position -> Axes.ORDER.indexOf(position.axis())))
                .toList();
    }
}
