package com.politest.figure.adapter.out.persistence;

import com.politest.figure.application.port.out.FigureCatalog;
import com.politest.figure.domain.model.Axes;
import com.politest.figure.domain.model.DatasetInfo;
import com.politest.figure.domain.model.Figure;
import com.politest.figure.domain.model.FigureWithPositions;
import com.politest.figure.domain.model.Position;
import java.util.Comparator;
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
    public List<FigureWithPositions> findAllActive(String lang) {
        List<Figure> figures = jdbc.sql("""
                        SELECT f.code, t.name, t.description, f.birth_year, f.death_year, f.color
                        FROM figure f JOIN figure_text t ON t.figure_code = f.code AND t.lang = :lang
                        WHERE f.active = 1
                        ORDER BY f.sort_order
                        """)
                .param("lang", lang)
                .query((rs, n) -> new Figure(rs.getString("code"), rs.getString("name"),
                        rs.getString("description"), rs.getInt("birth_year"), rs.getInt("death_year"),
                        rs.getString("color")))
                .list();

        record Row(String figureCode, Position position) {
        }
        Map<String, List<Position>> byFigure = jdbc.sql("""
                        SELECT fp.figure_code, fp.axis_code, fp.right_pct, fp.confidence, fp.note
                        FROM figure_position fp JOIN figure f ON f.code = fp.figure_code
                        WHERE f.active = 1
                        """)
                .query((rs, n) -> new Row(rs.getString("figure_code"),
                        new Position(rs.getString("axis_code"), rs.getInt("right_pct"),
                                rs.getInt("confidence"), rs.getString("note"))))
                .list().stream()
                .collect(Collectors.groupingBy(Row::figureCode, LinkedHashMap::new,
                        Collectors.mapping(Row::position, Collectors.toList())));

        return figures.stream()
                .map(figure -> new FigureWithPositions(figure,
                        sorted(byFigure.getOrDefault(figure.code(), List.of()))))
                .toList();
    }

    @Override
    public Optional<FigureWithPositions> findByCode(String code, String lang) {
        return findAllActive(lang).stream()
                .filter(figure -> figure.figure().code().equalsIgnoreCase(code))
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
