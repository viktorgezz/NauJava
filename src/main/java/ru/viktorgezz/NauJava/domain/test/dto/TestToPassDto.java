package ru.viktorgezz.NauJava.domain.test.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.viktorgezz.NauJava.domain.question.Type;

import java.util.List;

/**
 * DTO для передачи теста пользователю для прохождения.
 */
public record TestToPassDto(
        @NotNull Long idTest,
        @NotNull @Valid List<TestToPassDto.QuestionDto> questionsDto
) {

    public record QuestionDto(
            Long idQuestion,
            @NotBlank(message = "Текст вопроса не может быть пустым")
            String text,
            @NotNull(message = "Тип вопроса не может быть пустым")
            Type type,
            List<TestToPassDto.AnswerOptionDto> answerOptions
    ) {
    }

    public record AnswerOptionDto(
            Long idAnswerOption,
            String text
    ) {
    }
}
