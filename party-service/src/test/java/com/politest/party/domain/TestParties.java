package com.politest.party.domain;

import com.politest.party.domain.model.AxisCode;
import com.politest.party.domain.model.Confidence;
import com.politest.party.domain.model.Party;
import com.politest.party.domain.model.PartyCode;
import com.politest.party.domain.model.Percentage;
import com.politest.party.domain.model.Position;
import java.util.List;

/** Builders shared by the tests. */
public final class TestParties {

    private TestParties() {
    }

    public static Position position(AxisCode axis, int percentage) {
        return new Position(axis, Percentage.of(percentage), Confidence.PARTIAL, "note", null);
    }

    /** A party with positions on ECON and WELFARE only. */
    public static Party party(String code, int econ, int welfare) {
        return new Party(PartyCode.of(code), "Party " + code, "#336699",
                List.of(position(AxisCode.ECON, econ), position(AxisCode.WELFARE, welfare)));
    }
}
