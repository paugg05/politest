package com.politest.stats.config;

import com.politest.stats.application.port.out.CompletedTestStore;
import com.politest.stats.application.service.StatsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composition root: the only place where application services are wired to their ports.
 * A single bean resolves both inbound ports, because {@code StatsService} implements both.
 */
@Configuration
class ApplicationConfig {

    @Bean
    StatsService statsService(CompletedTestStore store) {
        return new StatsService(store);
    }
}
