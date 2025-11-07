package ru.viktorgezz.NauJava.domain.result.dto;

/**
 * DTO для представления участника теста в ответе.
 */
public record ParticipantDto(
        Long id,
        String username
) {
}
