package com.politest.politest.quiz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Estado del test de una persona (se guarda en la sesión HTTP).
 * No depende del idioma: solo guarda ids de pregunta y respuestas.
 * Respuesta omitida = clave presente con valor null.
 */
class QuizProgress implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Long> order;
    private final Map<Long, Integer> answers = new HashMap<>();
    private int index = 0;
    private String sharedResultId;
    private boolean completionPublished;

    QuizProgress(List<Long> order) {
        this.order = new ArrayList<>(order);
    }

    int total() {
        return order.size();
    }

    int index() {
        return index;
    }

    boolean finished() {
        return index >= order.size();
    }

    Long currentQuestionId() {
        return order.get(index);
    }

    boolean canGoBack() {
        return index > 0;
    }

    /** Registra la respuesta (de -2 a 2) o null si se omite, y avanza. */
    void answer(Integer value) {
        answers.put(order.get(index), value);
        index++;
    }

    void back() {
        if (index > 0) {
            index--;
            answers.remove(order.get(index));
        }
    }

    long answeredCount() {
        return answers.values().stream().filter(Objects::nonNull).count();
    }

    long skippedCount() {
        return answers.values().stream().filter(Objects::isNull).count();
    }

    Map<Long, Integer> answers() {
        return Collections.unmodifiableMap(answers);
    }

    /** Id del enlace ya creado para este test (null si aún no se ha compartido). */
    String sharedResultId() {
        return sharedResultId;
    }

    void sharedResultId(String id) {
        this.sharedResultId = id;
    }

    /** Whether the "test completed" event was already published for this test (it must happen once). */
    boolean completionPublished() {
        return completionPublished;
    }

    void completionPublished(boolean published) {
        this.completionPublished = published;
    }
}
