package com.politest.party.config;

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

    /**
     * A single bean: {@code AffinityService} implements both inbound ports, so Spring can resolve
     * either interface to it. Declaring one bean per interface would create ambiguous candidates.
     */
    @Bean
    AffinityService affinityService(PartyCatalog catalog) {
        return new AffinityService(catalog);
    }
}
