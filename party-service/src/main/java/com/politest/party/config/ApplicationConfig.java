package com.politest.party.config;

import com.politest.party.application.port.in.CompareAffinityUseCase;
import com.politest.party.application.port.in.QueryPartiesUseCase;
import com.politest.party.application.port.out.PartyCatalog;
import com.politest.party.application.service.AffinityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composition root: the only place where application services are wired to their ports.
 * Keeping it here leaves the application layer free of framework annotations.
 */
@Configuration
class ApplicationConfig {

    @Bean
    AffinityService affinityService(PartyCatalog catalog) {
        return new AffinityService(catalog);
    }

    @Bean
    CompareAffinityUseCase compareAffinityUseCase(AffinityService service) {
        return service;
    }

    @Bean
    QueryPartiesUseCase queryPartiesUseCase(AffinityService service) {
        return service;
    }
}
