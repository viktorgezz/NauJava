package ru.viktorgezz.testing_system.domain.result.dto;

import ru.viktorgezz.testing_system.domain.test.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для метаданных результата прохождения теста.
 */
public record ResultMetadataResponseDto(
        Long id,
        Long idTest,
        Status status,
        String titleTest,
        BigDecimal point,
        BigDecimal pointMax,
        LocalDateTime completedAt,
        int timeSpentSeconds
) {
}
