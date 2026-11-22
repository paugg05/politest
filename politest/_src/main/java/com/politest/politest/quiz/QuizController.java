package com.politest.politest.quiz;

import com.politest.politest.affinity.AffinityService;
import com.politest.politest.catalog.CatalogService;
import com.politest.politest.catalog.CatalogService.QuestionView;
import com.politest.politest.result.ResultService;
import com.politest.politest.scoring.ScoringService;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/{lang:ca|es|en}/quiz")
class QuizController {

    private static final String SESSION_KEY = "quizProgress";

    private final CatalogService catalog;
    private final ScoringService scoring;
    private final ResultService results;
    private final AffinityService affinity;

    QuizController(CatalogService catalog, ScoringService scoring, ResultService results, AffinityService affinity) {
        this.catalog = catalog;
        this.scoring = scoring;
        this.results = results;
        this.affinity = affinity;
    }

    /** Empieza el test o continúa donde se quedó. */
    @GetMapping
    String start(@PathVariable String lang, HttpSession session) {
        QuizProgress progress = progress(session);
        return redirect(lang, progress.finished() ? "finish" : "question");
    }

    @GetMapping("/question")
    String question(@PathVariable String lang, HttpSession session, Model model) {
        QuizProgress progress = progress(session);
        if (progress.finished()) {
            return redirect(lang, "finish");
        }

        Optional<QuestionView> question = catalog.question(progress.currentQuestionId(), lang);
        if (question.isEmpty()) {
            // La pregunta ya no está activa: se salta.
            progress.answer(null);
            return redirect(lang, "question");
        }

        model.addAttribute("question", question.get());
        model.addAttribute("number", progress.index() + 1);
        model.addAttribute("total", progress.total());
        model.addAttribute("percent", progress.index() * 100 / progress.total());
        model.addAttribute("canGoBack", progress.canGoBack());
        return "question";
    }

    /**
     * Guarda la respuesta (value de -2 a 2) o la omisión (skip).
     * Se exige el id de la pregunta para ignorar envíos duplicados o desfasados.
     */
    @PostMapping("/answer")
    String answer(@PathVariable String lang,
                  @RequestParam Long questionId,
                  @RequestParam(required = false) Integer value,
                  @RequestParam(required = false) String skip,
                  HttpSession session) {
        QuizProgress progress = progress(session);
        if (!progress.finished() && progress.currentQuestionId().equals(questionId)) {
            if (skip != null) {
                progress.answer(null);
            } else if (value != null && value >= -2 && value <= 2) {
                progress.answer(value);
            }
        }
        return redirect(lang, progress.finished() ? "finish" : "question");
    }

    @PostMapping("/back")
    String back(@PathVariable String lang, HttpSession session) {
        progress(session).back();
        return redirect(lang, "question");
    }

    /** Resultado: gráfico de barras calculado con las respuestas de la sesión (no se guarda nada). */
    @GetMapping("/finish")
    String finish(@PathVariable String lang, HttpSession session, Model model) {
        QuizProgress progress = progress(session);
        if (!progress.finished()) {
            return redirect(lang, "question");
        }
        Map<String, Integer> scores = scoring.rightPercentages(progress.answers());
        model.addAttribute("bars", results.bars(scores, lang));
        model.addAttribute("profile", results.profile(scores, lang));
        // Llama al microservicio party-service; si no está disponible, no hay sección de afinidad.
        model.addAttribute("affinity", affinity.compare(scores, lang).orElse(null));
        model.addAttribute("answered", progress.answeredCount());
        model.addAttribute("skipped", progress.skippedCount());
        model.addAttribute("shared", progress.sharedResultId() != null);
        return "finish";
    }

    /** Crea (una sola vez por test) el enlace público y lleva a la página del resultado. */
    @PostMapping("/share")
    String share(@PathVariable String lang, HttpSession session) {
        QuizProgress progress = progress(session);
        if (!progress.finished()) {
            return redirect(lang, "question");
        }
        if (progress.sharedResultId() == null) {
            Map<String, Integer> scores = scoring.rightPercentages(progress.answers());
            progress.sharedResultId(results.share(scores));
        }
        return "redirect:/" + lang + "/result/" + progress.sharedResultId();
    }

    @PostMapping("/restart")
    String restart(@PathVariable String lang, HttpSession session) {
        session.removeAttribute(SESSION_KEY);
        return redirect(lang, "question");
    }

    private QuizProgress progress(HttpSession session) {
        QuizProgress progress = (QuizProgress) session.getAttribute(SESSION_KEY);
        if (progress == null) {
            List<Long> ids = new ArrayList<>(catalog.activeQuestionIds());
            Collections.shuffle(ids);
            progress = new QuizProgress(ids);
            session.setAttribute(SESSION_KEY, progress);
        }
        return progress;
    }

    private static String redirect(String lang, String page) {
        return "redirect:/" + lang + "/quiz/" + page;
    }
}
