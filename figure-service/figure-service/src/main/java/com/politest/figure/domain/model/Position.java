package com.politest.figure.domain.model;

/**
 * Position of a figure on one axis, with the confidence of the estimate.
 *
 * @param note reasoning behind the estimate
 */
public record Position(AxisCode axis, Percentage rightPct, Confidence confidence, String note) {

    public Position {
        if (axis == null || rightPct == null || confidence == null) {
            throw new IllegalArgumentException("axis, rightPct and confidence are required");
        }
    }
}
