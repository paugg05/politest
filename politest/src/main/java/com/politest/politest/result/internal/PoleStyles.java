package com.politest.politest.result.internal;

import java.util.HashMap;
import java.util.Map;

/**
 * Aspecto de cada uno de los 16 polos (8 ejes x 2): color e icono.
 * Los iconos son trazos SVG sencillos sobre una cuadrícula de 64x64; el código es propio y
 * no depende de ningún fichero de imagen. El color coincide con el de las barras (app.css).
 */
public final class PoleStyles {

    /** Color y trazos SVG (contenido interior de un &lt;svg viewBox="0 0 64 64"&gt;). */
    public record Style(String color, String icon) {
    }

    private static final Map<String, Style> STYLES = build();

    private PoleStyles() {
    }

    /** @param side "L" (polo izquierdo, 0 %) o "R" (polo derecho, 100 %) */
    public static Style of(String axisCode, String side) {
        Style style = STYLES.get(axisCode + ":" + side);
        return style != null ? style : new Style("#455a64", "<circle cx=\"32\" cy=\"32\" r=\"16\"/>");
    }

    private static Map<String, Style> build() {
        Map<String, Style> m = new HashMap<>();
        put(m, "ECON", "L", "#b3261e",
            "<circle cx=\"32\" cy=\"18\" r=\"7\"/><circle cx=\"13\" cy=\"26\" r=\"5\"/><circle cx=\"51\" cy=\"26\" r=\"5\"/><path d=\"M20 54v-6a12 12 0 0 1 24 0v6\"/><path d=\"M4 52v-4a9 9 0 0 1 9-9\"/><path d=\"M60 52v-4a9 9 0 0 0-9-9\"/>");
        put(m, "ECON", "R", "#8a5a00",
            "<path d=\"M8 50L24 34l12 8 20-24\"/><path d=\"M42 18h14v14\"/>");
        put(m, "WELFARE", "L", "#00796b",
            "<path d=\"M32 54C8 38 6 20 20 14c7-3 12 2 12 6 0-4 5-9 12-6 14 6 12 24-12 40z\"/>");
        put(m, "WELFARE", "R", "#6a1b9a",
            "<circle cx=\"32\" cy=\"13\" r=\"6\"/><path d=\"M32 21v21\"/><path d=\"M14 29l18-6 18 6\"/><path d=\"M32 42L20 58\"/><path d=\"M32 42l12 16\"/>");
        put(m, "LIBERTY", "L", "#1565c0",
            "<path d=\"M6 30L58 8 44 56 30 38z\"/><path d=\"M30 38L58 8\"/>");
        put(m, "LIBERTY", "R", "#8e2d00",
            "<path d=\"M32 6l22 8v18c0 14-10 22-22 26C20 54 10 46 10 32V14z\"/><path d=\"M22 32l7 7 14-15\"/>");
        put(m, "CULTURE", "L", "#8e24aa",
            "<path d=\"M32 8a16 16 0 0 1 9 29v7H23v-7A16 16 0 0 1 32 8z\"/><path d=\"M24 52h16\"/><path d=\"M27 58h10\"/>");
        put(m, "CULTURE", "R", "#7b1f1f",
            "<path d=\"M8 24L32 8l24 16z\"/><path d=\"M16 29v19\"/><path d=\"M28 29v19\"/><path d=\"M40 29v19\"/><path d=\"M52 29v19\"/><path d=\"M8 55h48\"/>");
        put(m, "NATION", "L", "#3949ab",
            "<circle cx=\"32\" cy=\"32\" r=\"24\"/><ellipse cx=\"32\" cy=\"32\" rx=\"10\" ry=\"24\"/><path d=\"M8 32h48\"/><path d=\"M13 19h38\"/><path d=\"M13 45h38\"/>");
        put(m, "NATION", "R", "#c25500",
            "<path d=\"M16 8v50\"/><path d=\"M16 10h34l-8 10 8 10H16\"/>");
        put(m, "ECOLOGY", "L", "#2e7d32",
            "<path d=\"M12 52C12 24 28 10 54 10c0 28-14 44-42 42z\"/><path d=\"M12 52l24-24\"/>");
        put(m, "ECOLOGY", "R", "#00838f",
            "<path d=\"M8 56V30l16 10V30l16 10V10h14v46z\"/>");
        put(m, "DEMOCRACY", "L", "#0277bd",
            "<rect x=\"10\" y=\"30\" width=\"44\" height=\"26\" rx=\"3\"/><path d=\"M22 30V10h20v20\"/><path d=\"M27 20l4 4 7-8\"/><path d=\"M24 40h16\"/>");
        put(m, "DEMOCRACY", "R", "#6d4c41",
            "<path d=\"M4 24L32 10l28 14-28 14z\"/><path d=\"M16 31v13c0 6 32 6 32 0V31\"/><path d=\"M60 24v16\"/>");
        put(m, "CHANGE", "L", "#ad1457",
            "<path d=\"M6 54h14V42h12V30h12V18h14\"/>");
        put(m, "CHANGE", "R", "#455a64",
            "<path d=\"M36 6L14 36h16l-4 22 24-32H34z\"/>");
        return Map.copyOf(m);
    }

    private static void put(Map<String, Style> m, String axis, String side, String color, String icon) {
        m.put(axis + ":" + side, new Style(color, icon));
    }
}
