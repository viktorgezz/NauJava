package ru.viktorgezz.testing_system.domain.user.service.intrf;

import ru.viktorgezz.testing_system.domain.user.User;

/**
 * Сервис для управления пользователями (создание, обновление).
 */
public interface UserCommandService {

    void save(User user);
}
