package com.politest.stats.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.politest.stats.domain.error.DomainValidationException;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CompletedTestTests {

    private static final String ID = "3f2b8c1e-5d4a-4c3b-9a7e-1b2c3d4e5f60";
    private static final String AT = "2026-11-25T14:30:00Z";

    @Test
    void buildsFromRawEventValues() {
        CompletedTest test = CompletedTest.of(ID, AT, "ca", Map.of("ECON", 70, "LIBERTY", 20));

        assertEquals(ID, test.id().toString());
        assertEquals(Instant.parse(AT), test.occurredAt());
        assertEquals(Language.CA, test.language());
        assertEquals(Percentage.of(70), test.scores().get(AxisCode.ECON));
        assertEquals(2, test.scores().size());
    }

    @Test
    void rejectsInvalidEventId() {
        assertThrows(DomainValidationException.class,
                () -> CompletedTest.of("not-a-uuid", AT, "es", Map.of("ECON", 50)));
        assertThrows(DomainValidationException.class,
                () -> CompletedTest.of(null, AT, "es", Map.of("ECON", 50)));
    }

    @Test
    void rejectsInvalidTimestamp() {
        assertThrows(DomainValidationException.class,
                () -> CompletedTest.of(ID, "yesterday", "es", Map.of("ECON", 50)));
        assertThrows(DomainValidationException.class,
                () -> CompletedTest.of(ID, null, "es", Map.of("ECON", 50)));
    }

    @Test
    void rejectsUnsupportedLanguage() {
        assertThrows(DomainValidationException.class,
                () -> CompletedTest.of(ID, AT, "fr", Map.of("ECON", 50)));
    }

    @Test
    void rejectsUnknownAxisOrOutOfRangeValue() {
        assertThrows(DomainValidationException.class,
                () -> CompletedTest.of(ID, AT, "es", Map.of("MAGIC", 50)));
        assertThrows(DomainValidationException.class,
                () -> CompletedTest.of(ID, AT, "es", Map.of("ECON", 101)));
        assertThrows(DomainValidationException.class,
                () -> CompletedTest.of(ID, AT, "es", Map.of("ECON", -1)));
    }

    @Test
    void rejectsMissingOrEmptyScores() {
        assertThrows(DomainValidationException.class, () -> CompletedTest.of(ID, AT, "es", Map.of()));
        assertThrows(DomainValidationException.class, () -> CompletedTest.of(ID, AT, "es", null));
    }

    @Test
    void scoresCannotBeModifiedAfterCreation() {
        CompletedTest test = CompletedTest.of(ID, AT, "en", Map.of("ECON", 50));
        assertThrows(UnsupportedOperationException.class,
                () -> test.scores().put(AxisCode.CHANGE, Percentage.of(10)));
    }
}
