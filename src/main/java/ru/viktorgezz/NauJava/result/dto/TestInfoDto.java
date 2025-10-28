package ru.viktorgezz.NauJava.result.dto;

/**
 * DTO для представления краткой информации о тесте в ответе.
 */
public record TestInfoDto(
        Long id,
        String title
) {
}

