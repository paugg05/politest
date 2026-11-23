package com.politest.party.domain.model;

/**
 * Position of a party on one axis, with the confidence of the estimate.
 *
 * @param note      reasoning behind the estimate
 * @param sourceUrl consulted source, may be null when the value is inferred from the party's record
 */
public record Position(AxisCode axis, Percentage rightPct, Confidence confidence, String note, String sourceUrl) {

    public Position {
        if (axis == null || rightPct == null || confidence == null) {
            throw new IllegalArgumentException("axis, rightPct and confidence are required");
        }
    }
}
