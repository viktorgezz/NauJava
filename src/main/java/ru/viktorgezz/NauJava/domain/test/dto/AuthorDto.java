package ru.viktorgezz.NauJava.domain.test.dto;

/**
 * DTO для представления автора теста в ответе.
 */
public record AuthorDto(
        Long id,
        String username
) {
}
