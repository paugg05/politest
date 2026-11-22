package com.politest.politest.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.politest.politest.catalog.CatalogService.QuestionScoring;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ScoreCalculatorTests {

    private static final List<String> AXES = List.of("A", "B");

    // Preguntas 1 y 2: eje A (direcciones opuestas); la 3: eje B.
    private static final Map<Long, QuestionScoring> INFO = Map.of(
            1L, new QuestionScoring("A", 1, 1.0),
            2L, new QuestionScoring("A", -1, 1.0),
            3L, new QuestionScoring("B", 1, 1.0));

    private static Map<String, Integer> compute(Map<Long, Integer> answers) {
        return ScoreCalculator.rightPercentages(answers, INFO, AXES);
    }

    @Test
    void withoutAnswersEveryAxisIsFifty() {
        assertEquals(Map.of("A", 50, "B", 50), compute(new HashMap<>()));
    }

    @Test
    void agreeingWithQuestionsThatPushRightMovesToRight() {
        Map<Long, Integer> answers = new HashMap<>();
        answers.put(1L, 2);
        answers.put(3L, 2);
        assertEquals(Map.of("A", 100, "B", 100), compute(answers));
    }

    @Test
    void disagreeingWithQuestionsThatPushRightMovesToLeft() {
        Map<Long, Integer> answers = new HashMap<>();
        answers.put(1L, -2);
        answers.put(3L, -2);
        assertEquals(Map.of("A", 0, "B", 0), compute(answers));
    }

    @Test
    void agreeingWithOppositeDirectionsCancelsOut() {
        Map<Long, Integer> answers = new HashMap<>();
        answers.put(1L, 2);
        answers.put(2L, 2);
        assertEquals(50, compute(answers).get("A").intValue());
    }

    @Test
    void agreeingWithAQuestionThatPushesLeftMovesToLeft() {
        Map<Long, Integer> answers = new HashMap<>();
        answers.put(2L, 2);
        assertEquals(0, compute(answers).get("A").intValue());
    }

    @Test
    void skippedQuestionsAreIgnored() {
        Map<Long, Integer> answers = new HashMap<>();
        answers.put(1L, 2);
        answers.put(2L, null);
        assertEquals(100, compute(answers).get("A").intValue());
    }

    @Test
    void neutralAnswersStayInTheMiddle() {
        Map<Long, Integer> answers = new HashMap<>();
        answers.put(1L, 0);
        answers.put(3L, 0);
        assertEquals(Map.of("A", 50, "B", 50), compute(answers));
    }

    @Test
    void mildAgreementGivesSeventyFive() {
        Map<Long, Integer> answers = new HashMap<>();
        answers.put(1L, 1);
        assertEquals(75, compute(answers).get("A").intValue());
    }
}
