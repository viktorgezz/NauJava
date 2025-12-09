package ru.viktorgezz.NauJava.domain.test.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.viktorgezz.NauJava.domain.question.Type;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO для обновления контента теста: включает идентификатор теста и набор вопросов с вариантами ответов.
 */
public record TestUpdateTestContentDto(
        @NotNull Long idTest,
        @NotNull @Valid List<QuestionDto> questions
) {

    public record QuestionDto(
            Long idQuestion,
            @NotBlank(message = "Текст вопроса не может быть пустым")
            String text,
            @NotNull(message = "Тип вопроса не может быть пустым")
            Type type,
            @NotNull(message = "Количество баллов не может быть пустым")
            @DecimalMin(value = "0.0", message = "Количество баллов должно быть не менее 0")
            BigDecimal point,
            @Size(max = 5, message = "Количество верных ответов на вопрос не может превышать 5")
            List<@Size(max = 150, message = "Длина ответа на вопрос не должна превышать 150 символов")
                    String> correctTextAnswer,
            @Valid List<AnswerOptionDto> answerOptions
    ) {
    }

    public record AnswerOptionDto(
            Long idAnswerOption,
            @NotBlank(message = "Текст ответа не может быть пустым")
            String text,
            boolean isCorrect,
            String explanation
    ) {
    }
}
