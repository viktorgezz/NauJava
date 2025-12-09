package ru.viktorgezz.NauJava.domain.user.service.intrf;

import ru.viktorgezz.NauJava.domain.user.User;

/**
 * Сервис для управления пользователями (создание, обновление).
 */
public interface UserCommandService {

    void save(User user);
}
