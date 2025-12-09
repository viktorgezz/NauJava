package ru.viktorgezz.NauJava.domain.result.dto;

import ru.viktorgezz.NauJava.domain.question.Type;
import ru.viktorgezz.NauJava.domain.result.Grade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для представления результата прохождения теста в ответе REST API.
 */
public record ResultResponseDto(
        Long id,
        Long idTest,
        BigDecimal score,
        BigDecimal scoreMax,
        Grade grade,
        int timeSpentSeconds,
        LocalDateTime completedAt,
        List<QuestionDto> questionsDto
) {

    public record QuestionDto(
            Long idQuestion,
            String text,
            Type type,
            List<UserAnswerResponseDto> userAnswersDto
    ) {
    }

    public record UserAnswerResponseDto(
            Long idAnswerOption,
            String textOption,
            boolean isSelected,
            boolean isCorrect,
            String userTextAnswer,
            List<String> textAnswersTrue
    ) {
    }
}
