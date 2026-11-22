package com.politest.politest.catalog;

import com.politest.politest.catalog.internal.Axis;
import com.politest.politest.catalog.internal.AxisRepository;
import com.politest.politest.catalog.internal.AxisText;
import com.politest.politest.catalog.internal.Question;
import com.politest.politest.catalog.internal.QuestionRepository;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * API pública del módulo "catalog". Los demás módulos solo ven estas clases;
 * las entidades y repositorios quedan en el subpaquete "internal".
 */
@Service
@Transactional(readOnly = true)
public class CatalogService {

    public static final String DEFAULT_LANG = "es";

    public record QuestionView(Long id, String axisCode, int direction, double weight, String text) {
    }

    public record AxisView(String code, String name, String leftLabel, String rightLabel) {
    }

    /** Lo mínimo que necesita el módulo de puntuación de una pregunta. */
    public record QuestionScoring(String axisCode, int direction, double weight) {
    }

    private final QuestionRepository questions;
    private final AxisRepository axes;

    public CatalogService(QuestionRepository questions, AxisRepository axes) {
        this.questions = questions;
        this.axes = axes;
    }

    public List<Long> activeQuestionIds() {
        return questions.findActiveIds();
    }

    public long countActiveQuestions() {
        return questions.countByActiveTrue();
    }

    public Optional<QuestionView> question(Long id, String lang) {
        return questions.findById(id)
                .filter(Question::isActive)
                .map(q -> new QuestionView(q.getId(), q.getAxisCode(), q.getDirection(), q.getWeight(),
                        pick(q.getStatements(), lang)));
    }

    public Map<Long, QuestionScoring> scoringInfo(Collection<Long> ids) {
        Map<Long, QuestionScoring> info = new HashMap<>();
        for (Question q : questions.findAllById(ids)) {
            info.put(q.getId(), new QuestionScoring(q.getAxisCode(), q.getDirection(), q.getWeight()));
        }
        return info;
    }

    public List<String> axisCodes() {
        return axes.findAllByOrderBySortOrderAsc().stream().map(Axis::getCode).toList();
    }

    public List<AxisView> axes(String lang) {
        return axes.findAllByOrderBySortOrderAsc().stream()
                .map(a -> toView(a, lang))
                .toList();
    }

    private static AxisView toView(Axis axis, String lang) {
        AxisText t = pick(axis.getTexts(), lang);
        return new AxisView(axis.getCode(), t.name(), t.leftLabel(), t.rightLabel());
    }

    private static <T> T pick(Map<String, T> byLang, String lang) {
        T value = byLang.get(lang);
        return value != null ? value : byLang.get(DEFAULT_LANG);
    }
}
