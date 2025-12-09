package ru.viktorgezz.NauJava.domain.test.dto;

import ru.viktorgezz.NauJava.domain.test.Status;

import java.util.Set;

/**
 * DTO для представления теста в ответе REST API.
 */
public record TestMetadataResponseDto(
        Long id,
        String title,
        String description,
        Status status,
        AuthorDto author,
        Set<String> namesTopics
) {

    /**
     * DTO для представления автора теста в ответе.
     */
    public record AuthorDto(
            Long id,
            String username
    ) {
    }
}
