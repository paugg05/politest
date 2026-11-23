package com.politest.party.adapter.in.web;

import com.politest.party.application.port.in.CompareAffinityUseCase;
import com.politest.party.application.port.in.QueryPartiesUseCase;
import com.politest.party.domain.model.DatasetInfo;
import com.politest.party.domain.model.Party;
import com.politest.party.domain.model.Position;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * JSON representations of the REST API. They are independent of the domain model, so the API contract
 * can be kept stable while the domain evolves.
 */
public final class ApiModels {

    private ApiModels() {
    }

    public record AffinityRequest(Map<String, Integer> scores, Map<String, Double> weights) {

        CompareAffinityUseCase.Query toQuery() {
            return new CompareAffinityUseCase.Query(scores, weights);
        }
    }

    public record DatasetView(String version, String status, String note, LocalDate updatedOn) {

        static DatasetView from(DatasetInfo info) {
            return new DatasetView(info.version(), info.status(), info.note(), info.updatedOn());
        }
    }

    public record PartyView(String code, String name, String color) {

        static PartyView from(Party party) {
            return new PartyView(party.code().value(), party.name(), party.color());
        }
    }

    public record PositionView(String axis, int rightPct, int confidence, String note, String sourceUrl) {

        static PositionView from(Position position) {
            return new PositionView(position.axis().name(), position.rightPct().value(),
                    position.confidence().level(), position.note(), position.sourceUrl());
        }
    }

    public record PartyWithPositionsView(PartyView party, List<PositionView> positions) {

        static PartyWithPositionsView from(Party source) {
            return new PartyWithPositionsView(PartyView.from(source),
                    source.positions().stream().map(PositionView::from).toList());
        }
    }

    public record PartiesResponse(DatasetView dataset, List<PartyWithPositionsView> parties) {

        static PartiesResponse from(QueryPartiesUseCase.Catalog catalog) {
            return new PartiesResponse(DatasetView.from(catalog.dataset()),
                    catalog.parties().stream().map(PartyWithPositionsView::from).toList());
        }
    }

    public record AxisGapView(String axis, int user, int party, int confidence, String note, String sourceUrl) {

        static AxisGapView from(CompareAffinityUseCase.AxisGap gap) {
            return new AxisGapView(gap.axis(), gap.user(), gap.party(), gap.confidence(), gap.note(),
                    gap.sourceUrl());
        }
    }

    public record PartyMatchView(String code, String name, String color, int affinity, List<AxisGapView> axes) {

        static PartyMatchView from(CompareAffinityUseCase.PartyMatch match) {
            return new PartyMatchView(match.code(), match.name(), match.color(), match.affinity(),
                    match.axes().stream().map(AxisGapView::from).toList());
        }
    }

    public record AffinityResponse(DatasetView dataset, List<PartyMatchView> matches) {

        static AffinityResponse from(CompareAffinityUseCase.Result result) {
            return new AffinityResponse(DatasetView.from(result.dataset()),
                    result.matches().stream().map(PartyMatchView::from).toList());
        }
    }
}
