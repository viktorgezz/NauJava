package ru.viktorgezz.testing_system.domain.user.service.intrf;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.viktorgezz.testing_system.domain.user.Role;
import ru.viktorgezz.testing_system.domain.user.User;
import ru.viktorgezz.testing_system.domain.user.dto.UserResponseDto;

import java.util.List;

/**
 * Сервис получения данных о User
 */
public interface UserQueryService extends UserDetailsService {

    /**
     * Находит и возвращает пользователя по его имени (username).
     * @param username имя пользователя, по которому ведется поиск.
     * @return Объект {@link User} (найденный пользователь).
     * @throws ru.viktorgezz.testing_system.exception.BusinessException если пользователь с указанным именем не найден.
     */
    User getByUsername(String username);

    /**
     * Поиск юзеров по роли
     * @param role роль пользователя
     * @return возвращает список пользователей
     */
    List<User> findAllByRole(Role role);

    long computeCountUsers();

    UserResponseDto getUserDtoFromSecurityContext();
}
