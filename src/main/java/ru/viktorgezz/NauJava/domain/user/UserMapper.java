package ru.viktorgezz.NauJava.domain.user;

import ru.viktorgezz.NauJava.auth.dto.RegistrationRequest;
import ru.viktorgezz.NauJava.domain.user.dto.UserResponseDto;

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

    /**
     * Конвертирует {@link RegistrationRequest} и {@link Role} в {User}.
     *
     * @param request данные при регистрации
     * @param role роль пользователя
     * @return DTO пользователя
     */
    public static User toUser(RegistrationRequest request, Role role) {
        return new User(
                request.username(),
                request.password(),
                role,
                true,
                false,
                false
        );
    }
}

