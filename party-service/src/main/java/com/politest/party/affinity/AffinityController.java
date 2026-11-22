package com.politest.party.affinity;

import com.politest.party.affinity.AffinityDtos.AffinityRequest;
import com.politest.party.affinity.AffinityDtos.AffinityResponse;
import com.politest.party.party.PartyModels.DatasetInfo;
import com.politest.party.party.PartyModels.PartyWithPositions;
import com.politest.party.party.PartyRepository;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
class AffinityController {

    record PartiesResponse(DatasetInfo dataset, List<PartyWithPositions> parties) {
    }

    private final AffinityService affinity;
    private final PartyRepository repository;

    AffinityController(AffinityService affinity, PartyRepository repository) {
        this.affinity = affinity;
        this.repository = repository;
    }

    @GetMapping("/parties")
    PartiesResponse parties() {
        return new PartiesResponse(repository.datasetInfo(), repository.findAllActive());
    }

    @GetMapping("/parties/{code}")
    ResponseEntity<PartyWithPositions> party(@PathVariable String code) {
        return repository.findByCode(code)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/affinity")
    AffinityResponse compare(@RequestBody AffinityRequest request) {
        return affinity.compare(request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
}
