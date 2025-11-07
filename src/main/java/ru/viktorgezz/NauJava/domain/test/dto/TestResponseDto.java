package ru.viktorgezz.NauJava.domain.test.dto;

import ru.viktorgezz.NauJava.domain.test.Status;

/**
 * DTO для представления теста в ответе REST API.
 */
public record TestResponseDto(
        Long id,
        String title,
        String description,
        Status status,
        AuthorDto author
) {
}
