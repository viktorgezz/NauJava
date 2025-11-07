package ru.viktorgezz.NauJava.domain.user.dto;

import ru.viktorgezz.NauJava.domain.user.Role;

/**
 * DTO для представления пользователя в ответе REST API.
 * Не содержит пароль в целях безопасности.
 */
public record UserResponseDto(
        Long id,
        String username,
        Role role
) {
}
