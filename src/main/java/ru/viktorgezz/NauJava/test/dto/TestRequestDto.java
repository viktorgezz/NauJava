package ru.viktorgezz.NauJava.test.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.viktorgezz.NauJava.question.Question;
import ru.viktorgezz.NauJava.test.Status;

import java.util.List;
import java.util.Set;

/**
 * @param description Может быть null
 * @param questions   Список вопросов может быть пустым, но не null
 * @param topicIds    Набор ID тем может быть пустым, но не null
 */
public record TestRequestDto(
        @NotBlank(message = "Название теста не может быть пустым") String title,
        String description,
        @NotNull(message = "Статус теста не может быть null") Status status,
        Long authorId,
        @NotNull(message = "Список вопросов не может быть null (может быть пустым)") List<Question> questions,
        @NotNull(message = "Набор ID тем не может быть null (может быть пустым)") Set<Long> topicIds
) {


    public TestRequestDto(
            String title,
            String description,
            Status status,
            Long authorId,
            List<Question> questions,
            Set<Long> topicIds
    ) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.authorId = authorId;
        this.questions = questions;
        this.topicIds = topicIds;
    }

}
