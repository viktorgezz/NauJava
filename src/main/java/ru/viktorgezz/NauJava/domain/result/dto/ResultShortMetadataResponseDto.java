package ru.viktorgezz.NauJava.domain.result.dto;

import java.math.BigDecimal;

/**
 * DTO для метаданных (в уменьшенем количестве) результата прохождения теста.
 */
public record ResultShortMetadataResponseDto(
        BigDecimal point,
        BigDecimal pointMax,
        int timeSpentSeconds
) {
}
