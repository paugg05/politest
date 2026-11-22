package com.politest.figure.adapter.in.web;

import com.politest.figure.application.port.in.CompareAffinityUseCase;
import com.politest.figure.application.port.in.QueryFiguresUseCase;
import com.politest.figure.domain.model.DatasetInfo;
import com.politest.figure.domain.model.Figure;
import com.politest.figure.domain.model.FigureWithPositions;
import com.politest.figure.domain.model.Position;
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

    public record FigureView(String code, String name, String description, int birthYear, int deathYear,
                             String color) {

        static FigureView from(Figure figure) {
            return new FigureView(figure.code(), figure.name(), figure.description(), figure.birthYear(),
                    figure.deathYear(), figure.color());
        }
    }

    public record PositionView(String axis, int rightPct, int confidence, String note) {

        static PositionView from(Position position) {
            return new PositionView(position.axis(), position.rightPct(), position.confidence(), position.note());
        }
    }

    public record FigureWithPositionsView(FigureView figure, List<PositionView> positions) {

        static FigureWithPositionsView from(FigureWithPositions source) {
            return new FigureWithPositionsView(FigureView.from(source.figure()),
                    source.positions().stream().map(PositionView::from).toList());
        }
    }

    public record FiguresResponse(DatasetView dataset, List<FigureWithPositionsView> figures) {

        static FiguresResponse from(QueryFiguresUseCase.Catalog catalog) {
            return new FiguresResponse(DatasetView.from(catalog.dataset()),
                    catalog.figures().stream().map(FigureWithPositionsView::from).toList());
        }
    }

    public record AxisGapView(String axis, int user, int figure, int confidence, String note) {

        static AxisGapView from(CompareAffinityUseCase.AxisGap gap) {
            return new AxisGapView(gap.axis(), gap.user(), gap.figure(), gap.confidence(), gap.note());
        }
    }

    public record FigureMatchView(String code, String name, String description, int birthYear, int deathYear,
                                  String color, int affinity, List<AxisGapView> axes) {

        static FigureMatchView from(CompareAffinityUseCase.FigureMatch match) {
            return new FigureMatchView(match.code(), match.name(), match.description(), match.birthYear(),
                    match.deathYear(), match.color(), match.affinity(),
                    match.axes().stream().map(AxisGapView::from).toList());
        }
    }

    public record AffinityResponse(DatasetView dataset, List<FigureMatchView> matches) {

        static AffinityResponse from(CompareAffinityUseCase.Result result) {
            return new AffinityResponse(DatasetView.from(result.dataset()),
                    result.matches().stream().map(FigureMatchView::from).toList());
        }
    }
}
