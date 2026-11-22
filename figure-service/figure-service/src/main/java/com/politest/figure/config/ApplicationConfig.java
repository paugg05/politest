package com.politest.figure.config;

import com.politest.figure.application.port.in.CompareAffinityUseCase;
import com.politest.figure.application.port.in.QueryFiguresUseCase;
import com.politest.figure.application.port.out.FigureCatalog;
import com.politest.figure.application.service.AffinityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composition root: the only place where application services are wired to their ports.
 * Keeping it here leaves the application layer free of framework annotations.
 */
@Configuration
class ApplicationConfig {

    @Bean
    AffinityService affinityService(FigureCatalog catalog) {
        return new AffinityService(catalog);
    }

    @Bean
    CompareAffinityUseCase compareAffinityUseCase(AffinityService service) {
        return service;
    }

    @Bean
    QueryFiguresUseCase queryFiguresUseCase(AffinityService service) {
        return service;
    }
}
