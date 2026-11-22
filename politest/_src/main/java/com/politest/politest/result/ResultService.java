package com.politest.politest.result;

import com.politest.politest.catalog.CatalogService;
import com.politest.politest.result.internal.PoleStyles;
import com.politest.politest.result.internal.SharedResult;
import com.politest.politest.result.internal.SharedResultRepository;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** API pública del módulo "result": guardar y recuperar resultados compartidos y preparar el gráfico. */
@Service
@Transactional
public class ResultService {

    /** Una fila del gráfico: dos polos con su porcentaje (suman 100). */
    public record AxisBar(String code, String name, String leftLabel, String rightLabel,
                          int leftPct, int rightPct,
                          String leftColor, String rightColor, String leftIcon, String rightIcon) {
    }

    /** Un eje solo cuenta como rasgo si se aleja al menos esto del 50 % (en puntos). */
    private static final int MIN_DEVIATION = 5;
    private static final int MAX_TRAITS = 3;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final SharedResultRepository repository;
    private final CatalogService catalog;
    private final MessageSource messages;

    public ResultService(SharedResultRepository repository, CatalogService catalog, MessageSource messages) {
        this.repository = repository;
        this.catalog = catalog;
        this.messages = messages;
    }

    /** Guarda el resultado y devuelve su id público (token aleatorio de 16 caracteres). */
    public String share(Map<String, Integer> rightPct) {
        return repository.save(new SharedResult(newId(), rightPct)).getId();
    }

    @Transactional(readOnly = true)
    public Optional<Map<String, Integer>> find(String id) {
        return repository.findById(id).map(r -> new LinkedHashMap<>(r.getRightPct()));
    }

    /** Prepara las barras con las etiquetas de cada eje en el idioma pedido. */
    @Transactional(readOnly = true)
    public List<AxisBar> bars(Map<String, Integer> rightPct, String lang) {
        return catalog.axes(lang).stream()
                .map(axis -> {
                    int right = Math.max(0, Math.min(100, rightPct.getOrDefault(axis.code(), 50)));
                    PoleStyles.Style left = PoleStyles.of(axis.code(), "L");
                    PoleStyles.Style rightStyle = PoleStyles.of(axis.code(), "R");
                    return new AxisBar(axis.code(), axis.name(), axis.leftLabel(), axis.rightLabel(),
                            100 - right, right, left.color(), rightStyle.color(), left.icon(), rightStyle.icon());
                })
                .toList();
    }

    /**
     * Calcula los 3 rasgos más fuertes (los ejes más alejados del 50 %), la bandera y el lema.
     * Los empates se resuelven por el orden de los ejes.
     */
    @Transactional(readOnly = true)
    public ResultProfile profile(Map<String, Integer> rightPct, String lang) {
        Locale locale = Locale.forLanguageTag(lang);

        record Candidate(CatalogService.AxisView axis, String side, int pct) {
        }

        List<Candidate> candidates = catalog.axes(lang).stream()
                .map(axis -> {
                    int right = Math.max(0, Math.min(100, rightPct.getOrDefault(axis.code(), 50)));
                    return right >= 50
                            ? new Candidate(axis, "R", right)
                            : new Candidate(axis, "L", 100 - right);
                })
                .filter(c -> c.pct() - 50 >= MIN_DEVIATION)
                .sorted(Comparator.comparingInt(Candidate::pct).reversed()) // estable: respeta el orden de los ejes
                .limit(MAX_TRAITS)
                .toList();

        List<ResultProfile.Trait> traits = new ArrayList<>();
        for (Candidate c : candidates) {
            PoleStyles.Style style = PoleStyles.of(c.axis().code(), c.side());
            String key = "pole." + c.axis().code() + "." + c.side();
            traits.add(new ResultProfile.Trait(
                    c.axis().code(), c.side(),
                    c.side().equals("L") ? c.axis().leftLabel() : c.axis().rightLabel(),
                    c.pct(), level(c.pct()), style.color(), style.icon(),
                    messages.getMessage(key + ".word", null, locale),
                    messages.getMessage(key + ".meaning", null, locale)));
        }

        List<ResultProfile.FlagBand> bands = new ArrayList<>();
        int n = traits.size();
        for (int i = 0; i < n; i++) {
            int y = i * FLAG_HEIGHT / n;
            int next = (i + 1) * FLAG_HEIGHT / n;
            bands.add(new ResultProfile.FlagBand(traits.get(i).color(), y, next - y));
        }

        return new ResultProfile(List.copyOf(traits), List.copyOf(bands), slogan(traits, locale));
    }

    private static final int FLAG_HEIGHT = 200;

    private static String level(int pct) {
        return pct >= 85 ? "high" : pct >= 70 ? "mid" : "low";
    }

    private String slogan(List<ResultProfile.Trait> traits, Locale locale) {
        if (traits.isEmpty()) {
            return "";
        }
        Object[] words = traits.stream().map(ResultProfile.Trait::word).toArray();
        String text = messages.getMessage("result.slogan." + traits.size(), words, locale);
        return text.substring(0, 1).toUpperCase(locale) + text.substring(1);
    }

    private static String newId() {
        byte[] bytes = new byte[12];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
