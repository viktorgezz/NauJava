package ru.viktorgezz.NauJava.domain.user;

import ru.viktorgezz.NauJava.auth.dto.RegistrationRequest;
import ru.viktorgezz.NauJava.domain.user.dto.UserResponseOldDto;

/**
 * Маппер для конвертации {@link User} в DTO.
 */
public class UserMapper {

    /**
     * Конвертирует {@link User} в {@link UserResponseOldDto}.
     * Пароль не включается в DTO в целях безопасности.
     *
     * @param user модель пользователя
     * @return DTO пользователя
     */
    public static UserResponseOldDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponseOldDto(
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

