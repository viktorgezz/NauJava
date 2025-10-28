package ru.viktorgezz.NauJava.user.dto;

import ru.viktorgezz.NauJava.user.Role;

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

