package com.politest.politest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** Comprueba que los tres idiomas tienen los mismos textos y que las plantillas no usan claves inexistentes. */
class MessagesConsistencyTests {

    private static Properties load(String file) throws IOException {
        try (InputStream in = MessagesConsistencyTests.class.getResourceAsStream("/" + file)) {
            assertTrue(in != null, "No existe " + file);
            Properties p = new Properties();
            p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            return p;
        }
    }

    @Test
    void allLanguagesHaveTheSameKeys() throws IOException {
        Set<String> es = new TreeSet<>(load("messages_es.properties").stringPropertyNames());
        assertEquals(es, new TreeSet<>(load("messages_ca.properties").stringPropertyNames()), "ca difiere de es");
        assertEquals(es, new TreeSet<>(load("messages_en.properties").stringPropertyNames()), "en difiere de es");
    }

    @Test
    void messagesDoNotUseAsciiApostrophes() throws IOException {
        // En textos con parámetros ({0}) una comilla simple rompe MessageFormat: se usa ’ (U+2019).
        for (String file : new String[] {"messages_es.properties", "messages_ca.properties", "messages_en.properties"}) {
            load(file).forEach((k, v) -> assertTrue(!v.toString().contains("'"), file + " -> " + k));
        }
    }

    @Test
    void templatesOnlyUseExistingKeys() throws IOException {
        Properties es = load("messages_es.properties");
        Pattern key = Pattern.compile("#\\{([A-Za-z0-9_.]+)");
        Path templates = Path.of("src/main/resources/templates");
        try (Stream<Path> files = Files.walk(templates)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".html")).toList()) {
                Matcher m = key.matcher(Files.readString(file, StandardCharsets.UTF_8));
                while (m.find()) {
                    assertTrue(es.containsKey(m.group(1)), file + " usa la clave inexistente " + m.group(1));
                }
            }
        }
    }

    @Test
    void everyPoleHasWordMeaningAndIcon() throws IOException {
        String[] axes = {"ECON", "WELFARE", "LIBERTY", "CULTURE", "NATION", "ECOLOGY", "DEMOCRACY", "CHANGE"};
        for (String file : new String[] {"messages_es.properties", "messages_ca.properties", "messages_en.properties"}) {
            Properties p = load(file);
            for (String axis : axes) {
                for (String side : new String[] {"L", "R"}) {
                    String key = "pole." + axis + "." + side;
                    assertTrue(p.containsKey(key + ".word"), file + " falta " + key + ".word");
                    assertTrue(p.containsKey(key + ".meaning"), file + " falta " + key + ".meaning");
                    String icon = com.politest.politest.result.internal.PoleStyles.of(axis, side).icon();
                    assertTrue(!"<circle cx=\"32\" cy=\"32\" r=\"16\"/>".equals(icon), "Sin icono propio: " + key);
                }
            }
            for (String key : new String[] {"result.slogan.1", "result.slogan.2", "result.slogan.3",
                    "result.level.high", "result.level.mid", "result.level.low"}) {
                assertTrue(p.containsKey(key), file + " falta " + key);
            }
        }
    }
}
