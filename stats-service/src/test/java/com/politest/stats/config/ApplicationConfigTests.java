package com.politest.stats.config;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.politest.stats.application.InMemoryCompletedTestStore;
import com.politest.stats.application.port.in.QueryStatsUseCase;
import com.politest.stats.application.port.in.RegisterCompletedTestUseCase;
import com.politest.stats.application.port.out.CompletedTestStore;
import com.politest.stats.application.service.StatsService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/** Checks the wiring of the composition root without a database: every port must resolve to exactly one bean. */
class ApplicationConfigTests {

    @Test
    void inboundPortsResolveToTheSameSingleService() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.register(ApplicationConfig.class);
            context.registerBean(CompletedTestStore.class, InMemoryCompletedTestStore::new);
            context.refresh();

            StatsService service = context.getBean(StatsService.class);
            assertSame(service, context.getBean(RegisterCompletedTestUseCase.class));
            assertSame(service, context.getBean(QueryStatsUseCase.class));
        }
    }
}
