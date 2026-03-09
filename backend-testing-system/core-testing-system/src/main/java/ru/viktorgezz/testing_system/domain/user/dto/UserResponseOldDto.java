package ru.viktorgezz.testing_system.domain.user.dto;

import ru.viktorgezz.testing_system.domain.user.Role;

/**
 * DTO для представления пользователя в ответе REST API.
 * Не содержит пароль в целях безопасности.
 */
public record UserResponseOldDto(
        Long id,
        String username,
        Role role
) {
}
