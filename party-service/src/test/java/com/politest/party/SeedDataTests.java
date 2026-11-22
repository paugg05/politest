package com.politest.party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/** Comprueba el fichero de datos iniciales sin necesitar base de datos: 5 partidos x 8 ejes. */
class SeedDataTests {

    @Test
    void everyPartyHasAllEightAxes() throws IOException {
        String sql = Files.readString(Path.of("src/main/resources/db/migration/V2__seed_parties.sql"), StandardCharsets.UTF_8);
        Matcher m = Pattern.compile("\\('([A-Z_]+)', '(ECON|WELFARE|LIBERTY|CULTURE|NATION|ECOLOGY|DEMOCRACY|CHANGE)', (\\d+), ([123]),")
                .matcher(sql);
        int count = 0;
        while (m.find()) {
            count++;
            int pct = Integer.parseInt(m.group(3));
            assertTrue(pct >= 0 && pct <= 100, m.group());
        }
        assertEquals(40, count, "5 partidos x 8 ejes");
    }

    @Test
    void regionalPartiesHaveAllEightAxes() throws IOException {
        String sql = Files.readString(Path.of("src/main/resources/db/migration/V3__add_regional_parties.sql"), StandardCharsets.UTF_8);
        Matcher m = Pattern.compile("\\('([A-Z_]+)', '(ECON|WELFARE|LIBERTY|CULTURE|NATION|ECOLOGY|DEMOCRACY|CHANGE)', (\\d+), ([123]),")
                .matcher(sql);
        int count = 0;
        while (m.find()) {
            count++;
            int pct = Integer.parseInt(m.group(3));
            assertTrue(pct >= 0 && pct <= 100, m.group());
        }
        assertEquals(56, count, "7 partidos x 8 ejes");
    }
}
