package ru.viktorgezz.NauJava.result.dto;

import ru.viktorgezz.NauJava.result.Grade;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для представления результата прохождения теста в ответе REST API.
 */
public record ResultResponseDto(
        Long id,
        BigDecimal score,
        Grade grade,
        int timeSpentSeconds,
        LocalDateTime completedAt,
        ParticipantDto participant,
        TestInfoDto test
) {
}

