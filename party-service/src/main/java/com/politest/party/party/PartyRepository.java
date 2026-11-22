package com.politest.party.party;

import com.politest.party.party.PartyModels.DatasetInfo;
import com.politest.party.party.PartyModels.Party;
import com.politest.party.party.PartyModels.PartyWithPositions;
import com.politest.party.party.PartyModels.Position;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PartyRepository {

    private final JdbcClient jdbc;

    public PartyRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

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
                .map(p -> new PartyWithPositions(p, sorted(byParty.getOrDefault(p.code(), List.of()))))
                .toList();
    }

    public Optional<PartyWithPositions> findByCode(String code) {
        return findAllActive().stream().filter(p -> p.party().code().equalsIgnoreCase(code)).findFirst();
    }

    public DatasetInfo datasetInfo() {
        return jdbc.sql("SELECT version, status, note, updated_on FROM dataset_info WHERE id = 1")
                .query((rs, n) -> new DatasetInfo(rs.getString("version"), rs.getString("status"),
                        rs.getString("note"), rs.getDate("updated_on").toLocalDate()))
                .single();
    }

    private static List<Position> sorted(List<Position> positions) {
        return positions.stream()
                .sorted(Comparator.comparingInt(p -> Axes.ORDER.indexOf(p.axis())))
                .toList();
    }
}
