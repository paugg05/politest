package com.politest.figure.adapter.in.web;

import com.politest.figure.adapter.in.web.ApiModels.AffinityRequest;
import com.politest.figure.adapter.in.web.ApiModels.AffinityResponse;
import com.politest.figure.adapter.in.web.ApiModels.FigureWithPositionsView;
import com.politest.figure.adapter.in.web.ApiModels.FiguresResponse;
import com.politest.figure.application.InvalidAffinityRequestException;
import com.politest.figure.application.port.in.CompareAffinityUseCase;
import com.politest.figure.application.port.in.QueryFiguresUseCase;
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

/** Inbound adapter: exposes the use cases over REST. It only talks to the inbound ports. */
@RestController
@RequestMapping("/api/v1")
class AffinityController {

    private final CompareAffinityUseCase compareAffinity;
    private final QueryFiguresUseCase queryFigures;

    AffinityController(CompareAffinityUseCase compareAffinity, QueryFiguresUseCase queryFigures) {
        this.compareAffinity = compareAffinity;
        this.queryFigures = queryFigures;
    }

    @GetMapping("/figures")
    FiguresResponse figures(@RequestParam(defaultValue = "es") String lang) {
        return FiguresResponse.from(queryFigures.listFigures(lang));
    }

    @GetMapping("/figures/{code}")
    ResponseEntity<FigureWithPositionsView> figure(@PathVariable String code,
                                                   @RequestParam(defaultValue = "es") String lang) {
        return queryFigures.findFigure(code, lang)
                .map(FigureWithPositionsView::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/affinity")
    AffinityResponse compare(@RequestBody AffinityRequest request, @RequestParam(defaultValue = "es") String lang) {
        return AffinityResponse.from(compareAffinity.compare(request.toQuery(), lang));
    }

    @ExceptionHandler(InvalidAffinityRequestException.class)
    ResponseEntity<Map<String, String>> badRequest(InvalidAffinityRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
}
