package com.politest.figure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/** Comprueba el fichero de datos iniciales sin necesitar base de datos. */
class SeedDataTests {

    private static String seed() throws IOException {
        return Files.readString(Path.of("src/main/resources/db/migration/V2__seed_figures.sql"), StandardCharsets.UTF_8);
    }

    @Test
    void everyFigureHasTextsInThreeLanguages() throws IOException {
        Matcher m = Pattern.compile("\\('([A-Z_]+)', '(es|ca|en)', '").matcher(seed());
        int count = 0;
        while (m.find()) {
            count++;
        }
        assertEquals(60, count, "20 personajes x 3 idiomas");
    }

    @Test
    void positionsAreInRange() throws IOException {
        Matcher m = Pattern.compile("\\('([A-Z_]+)', '(ECON|WELFARE|LIBERTY|CULTURE|NATION|ECOLOGY|DEMOCRACY|CHANGE)', (\\d+), ([123]),")
                .matcher(seed());
        int count = 0;
        while (m.find()) {
            count++;
            int pct = Integer.parseInt(m.group(3));
            assertTrue(pct >= 0 && pct <= 100, m.group());
        }
        assertTrue(count >= 20 * 6, "cada personaje debería tener al menos 6 ejes: " + count);
    }
}
