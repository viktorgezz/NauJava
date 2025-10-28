package ru.viktorgezz.NauJava.user.dto;

import ru.viktorgezz.NauJava.user.User;

/**
 * Маппер для конвертации {@link User} в DTO.
 */
public class UserMapper {

    /**
     * Конвертирует {@link User} в {@link UserResponseDto}.
     * Пароль не включается в DTO в целях безопасности.
     *
     * @param user модель пользователя
     * @return DTO пользователя
     */
    public static UserResponseDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
    }
}

