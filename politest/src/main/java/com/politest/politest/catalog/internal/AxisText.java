package com.politest.politest.catalog.internal;

import jakarta.persistence.Embeddable;

@Embeddable
public record AxisText(String name, String leftLabel, String rightLabel) {
}
