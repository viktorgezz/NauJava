package ru.viktorgezz.NauJava.domain.report.dto;

import ru.viktorgezz.NauJava.domain.result.Grade;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для представления результатов прохождений тестов в отчете во время ответа по REST API.
 */
public record ResultResponse(
        BigDecimal score,
        Grade grade,
        int timeSpentSeconds,
        LocalDateTime completedAt,
        String usernameParticipant,
        String titleTest
) {
}
