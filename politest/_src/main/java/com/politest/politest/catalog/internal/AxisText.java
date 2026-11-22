package com.politest.politest.catalog.internal;

import jakarta.persistence.Embeddable;

/** Textos de un eje en un idioma (una fila de axis_translation). */
@Embeddable
public record AxisText(String name, String leftLabel, String rightLabel) {
}
