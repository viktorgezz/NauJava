package ru.viktorgezz.testing_system.domain.user.dto;

import ru.viktorgezz.testing_system.domain.user.Role;

public record UserResponseDto(
        String username,
        Role role
) {
}
