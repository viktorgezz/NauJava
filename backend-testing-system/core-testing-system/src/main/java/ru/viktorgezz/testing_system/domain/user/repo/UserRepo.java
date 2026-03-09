package ru.viktorgezz.testing_system.domain.user.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.viktorgezz.testing_system.domain.user.Role;
import ru.viktorgezz.testing_system.domain.user.User;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для доступа к сущностям {@link User}.
 */
public interface UserRepo extends CrudRepository<User, Long> {

    /**
     * Поиск пользователя по имени
     *
     * @param username имя пользователя
     * @return Возвращает в Optional юзера
     */
    Optional<User> findByUsername(String username);

    /**
     * Поиск юзеров по роли
     *
     * @param role роль пользователя
     * @return возвращает список пользователей
     */
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findAllByRole(@Param("role") Role role);

    @Query("SELECT COUNT(u) FROM User u")
    long getCountUsers();
}
