package com.politest.politest.result;

import java.util.List;

/**
 * Perfil de una persona a partir de sus porcentajes: los rasgos más marcados, la bandera
 * (todo calculado, sin imágenes) y el lema que la acompaña.
 *
 * @param top      hasta 3 rasgos, del más fuerte al más débil (vacío si el perfil es equilibrado)
 * @param bands    franjas de la bandera (una por rasgo)
 * @param slogan   lema con las palabras de los rasgos, p. ej. «Igualdad, libertad y tradición»
 */
public record ResultProfile(List<Trait> top, List<FlagBand> bands, String slogan) {

    /**
     * Un rasgo: el polo hacia el que más te inclinas en un eje.
     *
     * @param pct   inclinación hacia ese polo (51-100)
     * @param level intensidad: "low", "mid" o "high"
     * @param icon  trazos SVG del logo del polo (viewBox 64x64)
     */
    public record Trait(String axisCode, String side, String poleName, int pct, String level,
                        String color, String icon, String word, String meaning) {
    }

    /** Franja horizontal de la bandera (el lienzo mide 300x200). */
    public record FlagBand(String color, int y, int height) {
    }

    public boolean balanced() {
        return top.isEmpty();
    }

    /** Logo del rasgo principal, que va en el escudo central de la bandera. */
    public Trait main() {
        return top.isEmpty() ? null : top.get(0);
    }
}
