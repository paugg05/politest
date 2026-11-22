package com.politest.party.domain.model;

/**
 * Position of a party on one axis.
 *
 * @param axis       axis code, one of {@link Axes#CODES}
 * @param rightPct   0 is the left pole of the axis, 100 the right pole (same scale as the web application)
 * @param confidence 1 weak basis, 2 partial basis, 3 solid basis
 * @param note       reasoning behind the estimate
 * @param sourceUrl  consulted source, may be null when the value is inferred from the party's record
 */
public record Position(String axis, int rightPct, int confidence, String note, String sourceUrl) {
}
