package ru.viktorgezz.NauJava.domain.report.dto;

import ru.viktorgezz.NauJava.domain.report.StatusReport;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для представления отчета в ответе REST API.
 */
public record ReportUserCountResultsResponse(
        Long id,
        StatusReport status,
        Long countUsers,
        List<ResultResponse> resultResponses,
        Long timeSpentSearchingForUsersMillis,
        Long timeSpentSearchingForResultsMillis,
        Long timeSpentCommonMillis,
        LocalDateTime completedAt
) {
}
