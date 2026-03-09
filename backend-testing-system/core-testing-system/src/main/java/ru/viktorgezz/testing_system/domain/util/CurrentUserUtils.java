package ru.viktorgezz.testing_system.domain.util;

import ru.viktorgezz.testing_system.domain.user.User;

/**
 * Утилитный класс для получения текущего аутентифицированного пользователя из SecurityContext.
 */
public class CurrentUserUtils {

    private CurrentUserUtils() {
    }

    /**
     * Получает текущего аутентифицированного пользователя из внешнего модуля.
     *
     * @return объект {@link User} текущего пользователя
     * @throws RuntimeException если пользователь не аутентифицирован
     */
    public static User getCurrentUser() {
        return (User) ru.viktorgezz.security.util.CurrentUserUtils.getCurrentUser();
    }
}
