package com.politest.figure.domain.model;

/**
 * Position of a figure on one axis.
 *
 * @param axis       axis code, one of {@link Axes#CODES}
 * @param rightPct   0 is the left pole of the axis, 100 the right pole (same scale as the web application)
 * @param confidence 1 weak basis, 2 partial basis, 3 solid basis
 * @param note       reasoning behind the estimate
 */
public record Position(String axis, int rightPct, int confidence, String note) {
}
