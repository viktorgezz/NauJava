package ru.viktorgezz.NauJava.domain.user.dto;

import ru.viktorgezz.NauJava.domain.user.Role;

public record UserResponseDto(
        String username,
        Role role
) {
}
