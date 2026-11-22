package com.politest.party.party;

import java.util.List;
import java.util.Set;

/**
 * Códigos de los 8 ejes. Es parte del CONTRATO con la aplicación principal (que tiene su propio catálogo):
 * si algún día cambian los ejes, hay que cambiarlos en los dos lados.
 */
public final class Axes {

    public static final List<String> ORDER =
            List.of("ECON", "WELFARE", "LIBERTY", "CULTURE", "NATION", "ECOLOGY", "DEMOCRACY", "CHANGE");
    public static final Set<String> CODES = Set.copyOf(ORDER);

    private Axes() {
    }
}
