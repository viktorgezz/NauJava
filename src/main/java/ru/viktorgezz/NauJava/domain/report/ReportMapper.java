package ru.viktorgezz.NauJava.domain.report;

import ru.viktorgezz.NauJava.domain.report.dto.ResultResponse;
import ru.viktorgezz.NauJava.domain.report.dto.ReportUserCountResultsResponse;
import ru.viktorgezz.NauJava.domain.result.Result;

import java.util.List;

/**
 * Маппер для конвертации в домене отчетов
 */
public class ReportMapper {

    private ReportMapper() {
    }

    public static ReportUserCountResultsResponse toDto(
            ReportUserCountResultsModel model,
            List<ResultResponse> resultResponses
    ) {
        return new ReportUserCountResultsResponse(
                model.getId(),
                model.getStatus(),
                model.getCountUsers(),
                resultResponses,
                model.getTimeSpentSearchingForUsersMillis(),
                model.getTimeSpentSearchingForResultsMillis(),
                model.getTimeSpentCommonMillis(),
                model.getCompletedAt()
        );
    }

    public static ResultResponse toDto(
            Result result,
            String usernameParticipant,
            String titleTest
    ) {
        return new ResultResponse(
                result.getScore(),
                result.getGrade(),
                result.getTimeSpentSeconds(),
                result.getCompletedAt(),
                usernameParticipant,
                titleTest
        );
    }
}
