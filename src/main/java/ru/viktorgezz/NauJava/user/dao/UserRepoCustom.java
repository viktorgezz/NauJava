package ru.viktorgezz.NauJava.user.dao;

import org.springframework.data.repository.query.Param;
import ru.viktorgezz.NauJava.user.Role;
import ru.viktorgezz.NauJava.user.User;

import java.util.List;
import java.util.Optional;

public interface UserRepoCustom {

    /**
     * Поиск пользователя по имени
     * @param username имя пользователя
     * @return Возвращает в Optional юзера
     */
    Optional<User> findByUsername(String username);

    /**
     * Поиск юзеров по роли
     * @param role роль пользователя
     * @return возвращает список пользователей
     */
    List<User> findAllByRole(@Param("role") Role role);
}
