package com.politest.figure.figure;

import java.util.List;
import java.util.Set;

/**
 * Códigos de los 8 ejes. Es parte del CONTRATO con la aplicación principal (que tiene su propio catálogo):
 * si algún día cambian los ejes, hay que cambiarlos en todos los servicios.
 */
public final class Axes {

    public static final List<String> ORDER =
            List.of("ECON", "WELFARE", "LIBERTY", "CULTURE", "NATION", "ECOLOGY", "DEMOCRACY", "CHANGE");
    public static final Set<String> CODES = Set.copyOf(ORDER);
    /** Idiomas con textos en la base de datos. */
    public static final Set<String> LANGS = Set.of("es", "ca", "en");

    private Axes() {
    }
}
