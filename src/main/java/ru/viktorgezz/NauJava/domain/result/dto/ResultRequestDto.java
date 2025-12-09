package ru.viktorgezz.NauJava.domain.result.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * DTO для запроса результата прохождения теста.
 */
public record ResultRequestDto(
        int timeSpentSeconds,
        @NotNull Long idTest,
        @NotEmpty Map<Long, UserAnswerRequestDto> idQuestionToUserAnswers // ключ id вопроса
) {
    /**
     * DTO для ответа пользователя на вопрос.
     */
    public record UserAnswerRequestDto(
            @NotNull String textAnswerWritten,
            @NotNull List<Long> idsSelectedAnswerOption
    ) {
    }
}
