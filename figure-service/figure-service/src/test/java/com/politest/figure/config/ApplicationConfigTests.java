package com.politest.figure.config;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.politest.figure.application.port.in.CompareAffinityUseCase;
import com.politest.figure.application.port.in.QueryFiguresUseCase;
import com.politest.figure.application.port.out.FigureCatalog;
import com.politest.figure.application.service.AffinityService;
import com.politest.figure.domain.model.DatasetInfo;
import com.politest.figure.domain.model.Figure;
import com.politest.figure.domain.model.FigureCode;
import com.politest.figure.domain.model.Language;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/** Checks the wiring of the composition root without a database: every port must resolve to exactly one bean. */
class ApplicationConfigTests {

    private static final FigureCatalog EMPTY_CATALOG = new FigureCatalog() {
        @Override
        public List<Figure> findAllActive(Language language) {
            return List.of();
        }

        @Override
        public Optional<Figure> findByCode(FigureCode code, Language language) {
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
            context.registerBean(FigureCatalog.class, () -> EMPTY_CATALOG);
            context.refresh();

            AffinityService service = context.getBean(AffinityService.class);
            assertSame(service, context.getBean(CompareAffinityUseCase.class));
            assertSame(service, context.getBean(QueryFiguresUseCase.class));
        }
    }
}
