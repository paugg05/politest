package com.politest.party.config;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.politest.party.application.port.in.CompareAffinityUseCase;
import com.politest.party.application.port.in.QueryPartiesUseCase;
import com.politest.party.application.port.out.PartyCatalog;
import com.politest.party.application.service.AffinityService;
import com.politest.party.domain.model.DatasetInfo;
import com.politest.party.domain.model.PartyWithPositions;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/** Checks the wiring of the composition root without a database: every port must resolve to exactly one bean. */
class ApplicationConfigTests {

    private static final PartyCatalog EMPTY_CATALOG = new PartyCatalog() {
        @Override
        public List<PartyWithPositions> findAllActive() {
            return List.of();
        }

        @Override
        public Optional<PartyWithPositions> findByCode(String code) {
            return Optional.empty();
        }

        @Override
        public DatasetInfo datasetInfo() {
            return new DatasetInfo("test", "TEST", "", LocalDate.of(2026, 1, 1));
        }
    };

    @Test
    void inboundPortsResolveToTheSameSingleService() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.register(ApplicationConfig.class);
            context.registerBean(PartyCatalog.class, () -> EMPTY_CATALOG);
            context.refresh();

            AffinityService service = context.getBean(AffinityService.class);
            assertSame(service, context.getBean(CompareAffinityUseCase.class));
            assertSame(service, context.getBean(QueryPartiesUseCase.class));
        }
    }
}
