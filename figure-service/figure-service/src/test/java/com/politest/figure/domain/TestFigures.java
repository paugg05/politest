package com.politest.figure.domain;

import com.politest.figure.domain.model.AxisCode;
import com.politest.figure.domain.model.Confidence;
import com.politest.figure.domain.model.Figure;
import com.politest.figure.domain.model.FigureCode;
import com.politest.figure.domain.model.Percentage;
import com.politest.figure.domain.model.Position;
import java.util.List;

/** Builders shared by the tests. */
public final class TestFigures {

    private TestFigures() {
    }

    public static Position position(AxisCode axis, int percentage) {
        return new Position(axis, Percentage.of(percentage), Confidence.PARTIAL, "note");
    }

    /** A figure with positions on ECON and WELFARE only. */
    public static Figure figure(String code, int econ, int welfare) {
        return new Figure(FigureCode.of(code), "Figure " + code, "Description of " + code, 1900, 1980, "#663399",
                List.of(position(AxisCode.ECON, econ), position(AxisCode.WELFARE, welfare)));
    }
}
