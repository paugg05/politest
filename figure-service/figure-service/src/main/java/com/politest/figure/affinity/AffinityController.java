package com.politest.figure.affinity;

import com.politest.figure.affinity.AffinityDtos.AffinityRequest;
import com.politest.figure.affinity.AffinityDtos.AffinityResponse;
import com.politest.figure.figure.FigureModels.DatasetInfo;
import com.politest.figure.figure.FigureModels.FigureWithPositions;
import com.politest.figure.figure.FigureRepository;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
class AffinityController {

    record FiguresResponse(DatasetInfo dataset, List<FigureWithPositions> figures) {
    }

    private final AffinityService affinity;
    private final FigureRepository repository;

    AffinityController(AffinityService affinity, FigureRepository repository) {
        this.affinity = affinity;
        this.repository = repository;
    }

    @GetMapping("/figures")
    FiguresResponse figures(@RequestParam(defaultValue = "es") String lang) {
        return new FiguresResponse(repository.datasetInfo(), repository.findAllActive(lang));
    }

    @GetMapping("/figures/{code}")
    ResponseEntity<FigureWithPositions> figure(@PathVariable String code, @RequestParam(defaultValue = "es") String lang) {
        return repository.findByCode(code, lang)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/affinity")
    AffinityResponse compare(@RequestBody AffinityRequest request, @RequestParam(defaultValue = "es") String lang) {
        return affinity.compare(request, lang);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
}
