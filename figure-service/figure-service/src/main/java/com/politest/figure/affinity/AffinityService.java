package com.politest.figure.affinity;

import com.politest.figure.affinity.AffinityDtos.AffinityRequest;
import com.politest.figure.affinity.AffinityDtos.AffinityResponse;
import com.politest.figure.affinity.AffinityDtos.AxisGap;
import com.politest.figure.affinity.AffinityDtos.FigureMatch;
import com.politest.figure.figure.Axes;
import com.politest.figure.figure.FigureModels.FigureWithPositions;
import com.politest.figure.figure.FigureModels.Position;
import com.politest.figure.figure.FigureRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AffinityService {

    private final FigureRepository repository;

    public AffinityService(FigureRepository repository) {
        this.repository = repository;
    }

    /**
     * Compara los porcentajes de la persona con cada personaje y devuelve la lista ordenada de más a menos afín.
     * Los ejes en los que un personaje no tiene posición (porque no eran un tema de su época) se ignoran.
     */
    public AffinityResponse compare(AffinityRequest request, String lang) {
        validate(request, lang);
        Map<String, Integer> scores = request.scores();

        List<FigureMatch> matches = repository.findAllActive(lang).stream()
                .map(f -> toMatch(f, scores, request.weights()))
                .sorted(Comparator.comparingInt(FigureMatch::affinity).reversed())
                .toList();
        return new AffinityResponse(repository.datasetInfo(), matches);
    }

    private static FigureMatch toMatch(FigureWithPositions item, Map<String, Integer> scores,
                                       Map<String, Double> weights) {
        double affinity = AffinityCalculator.affinity(scores, item.asMap(), weights);
        List<AxisGap> gaps = item.positions().stream()
                .filter(p -> scores.containsKey(p.axis()))
                .map(p -> toGap(p, scores.get(p.axis())))
                .toList();
        var f = item.figure();
        return new FigureMatch(f.code(), f.name(), f.description(), f.birthYear(), f.deathYear(), f.color(),
                (int) Math.round(affinity), gaps);
    }

    private static AxisGap toGap(Position position, int user) {
        return new AxisGap(position.axis(), user, position.rightPct(), position.confidence(), position.note());
    }

    private static void validate(AffinityRequest request, String lang) {
        if (!Axes.LANGS.contains(lang)) {
            throw new IllegalArgumentException("Idioma no soportado: " + lang + " (usa es, ca o en).");
        }
        if (request == null || request.scores() == null || request.scores().isEmpty()) {
            throw new IllegalArgumentException("Falta «scores»: porcentajes por eje.");
        }
        for (Map.Entry<String, Integer> entry : request.scores().entrySet()) {
            if (!Axes.CODES.contains(entry.getKey())) {
                throw new IllegalArgumentException("Eje desconocido: " + entry.getKey());
            }
            if (entry.getValue() == null || entry.getValue() < 0 || entry.getValue() > 100) {
                throw new IllegalArgumentException("El porcentaje de " + entry.getKey() + " debe estar entre 0 y 100.");
            }
        }
        if (request.weights() != null) {
            for (Map.Entry<String, Double> entry : request.weights().entrySet()) {
                if (!Axes.CODES.contains(entry.getKey())) {
                    throw new IllegalArgumentException("Eje desconocido en «weights»: " + entry.getKey());
                }
                if (entry.getValue() == null || entry.getValue() < 0 || entry.getValue() > 10) {
                    throw new IllegalArgumentException("El peso de " + entry.getKey() + " debe estar entre 0 y 10.");
                }
            }
        }
    }
}
