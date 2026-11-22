package com.politest.figure.figure;

import com.politest.figure.figure.FigureModels.DatasetInfo;
import com.politest.figure.figure.FigureModels.Figure;
import com.politest.figure.figure.FigureModels.FigureWithPositions;
import com.politest.figure.figure.FigureModels.Position;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Acceso a la base de datos propia del servicio (JDBC sencillo: solo lecturas). */
@Repository
public class FigureRepository {

    private final JdbcClient jdbc;

    public FigureRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Personajes activos con textos en el idioma indicado (es, ca o en). */
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
                .map(f -> new FigureWithPositions(f, sorted(byFigure.getOrDefault(f.code(), List.of()))))
                .toList();
    }

    public Optional<FigureWithPositions> findByCode(String code, String lang) {
        return findAllActive(lang).stream().filter(f -> f.figure().code().equalsIgnoreCase(code)).findFirst();
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
