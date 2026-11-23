package com.politest.figure.adapter.out.persistence;

import com.politest.figure.application.port.out.FigureCatalog;
import com.politest.figure.domain.model.AxisCode;
import com.politest.figure.domain.model.Confidence;
import com.politest.figure.domain.model.DatasetInfo;
import com.politest.figure.domain.model.Figure;
import com.politest.figure.domain.model.FigureCode;
import com.politest.figure.domain.model.Language;
import com.politest.figure.domain.model.Percentage;
import com.politest.figure.domain.model.Position;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Outbound adapter: reads the figure catalogue from MySQL with {@link JdbcClient} (read-only queries). */
@Repository
class JdbcFigureCatalog implements FigureCatalog {

    private final JdbcClient jdbc;

    JdbcFigureCatalog(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Figure> findAllActive(Language language) {
        record FigureRow(String code, String name, String description, int birthYear, int deathYear, String color) {
        }
        List<FigureRow> rows = jdbc.sql("""
                        SELECT f.code, t.name, t.description, f.birth_year, f.death_year, f.color
                        FROM figure f JOIN figure_text t ON t.figure_code = f.code AND t.lang = :lang
                        WHERE f.active = 1
                        ORDER BY f.sort_order
                        """)
                .param("lang", language.code())
                .query((rs, n) -> new FigureRow(rs.getString("code"), rs.getString("name"),
                        rs.getString("description"), rs.getInt("birth_year"), rs.getInt("death_year"),
                        rs.getString("color")))
                .list();

        record PositionRow(String figureCode, Position position) {
        }
        Map<String, List<Position>> byFigure = jdbc.sql("""
                        SELECT fp.figure_code, fp.axis_code, fp.right_pct, fp.confidence, fp.note
                        FROM figure_position fp JOIN figure f ON f.code = fp.figure_code
                        WHERE f.active = 1
                        """)
                .query((rs, n) -> new PositionRow(rs.getString("figure_code"),
                        new Position(AxisCode.of(rs.getString("axis_code")),
                                Percentage.of(rs.getInt("right_pct")),
                                Confidence.ofLevel(rs.getInt("confidence")),
                                rs.getString("note"))))
                .list().stream()
                .collect(Collectors.groupingBy(PositionRow::figureCode, LinkedHashMap::new,
                        Collectors.mapping(PositionRow::position, Collectors.toList())));

        return rows.stream()
                .map(row -> new Figure(FigureCode.of(row.code()), row.name(), row.description(), row.birthYear(),
                        row.deathYear(), row.color(), byFigure.getOrDefault(row.code(), List.of())))
                .toList();
    }

    @Override
    public Optional<Figure> findByCode(FigureCode code, Language language) {
        return findAllActive(language).stream()
                .filter(figure -> figure.code().equals(code))
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
