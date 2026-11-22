package com.politest.party.adapter.in.web;

import com.politest.party.adapter.in.web.ApiModels.AffinityRequest;
import com.politest.party.adapter.in.web.ApiModels.AffinityResponse;
import com.politest.party.adapter.in.web.ApiModels.PartiesResponse;
import com.politest.party.adapter.in.web.ApiModels.PartyWithPositionsView;
import com.politest.party.application.InvalidAffinityRequestException;
import com.politest.party.application.port.in.CompareAffinityUseCase;
import com.politest.party.application.port.in.QueryPartiesUseCase;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Inbound adapter: exposes the use cases over REST. It only talks to the inbound ports. */
@RestController
@RequestMapping("/api/v1")
class AffinityController {

    private final CompareAffinityUseCase compareAffinity;
    private final QueryPartiesUseCase queryParties;

    AffinityController(CompareAffinityUseCase compareAffinity, QueryPartiesUseCase queryParties) {
        this.compareAffinity = compareAffinity;
        this.queryParties = queryParties;
    }

    @GetMapping("/parties")
    PartiesResponse parties() {
        return PartiesResponse.from(queryParties.listParties());
    }

    @GetMapping("/parties/{code}")
    ResponseEntity<PartyWithPositionsView> party(@PathVariable String code) {
        return queryParties.findParty(code)
                .map(PartyWithPositionsView::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/affinity")
    AffinityResponse compare(@RequestBody AffinityRequest request) {
        return AffinityResponse.from(compareAffinity.compare(request.toQuery()));
    }

    @ExceptionHandler(InvalidAffinityRequestException.class)
    ResponseEntity<Map<String, String>> badRequest(InvalidAffinityRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
}
