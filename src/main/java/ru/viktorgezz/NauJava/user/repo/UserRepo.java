package ru.viktorgezz.NauJava.user.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.viktorgezz.NauJava.user.Role;
import ru.viktorgezz.NauJava.user.User;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для доступа к сущностям {@link User}.
 */
@RepositoryRestResource(path = "users")
public interface UserRepo extends CrudRepository<User, Long> {

    /**
     * Поиск пользователя по имени
     * @param username имя пользователя
     * @return Возвращает в Optional юзера
     */
    Optional<User> findByUsername(String username);

    /**
     * Поиск id пользователя по имени
     * @param username имя пользователя
     * @return Возращает id пользователя
     */
    Optional<Long>  findIdByUsername(String username);

    /**
     * Поиск юзеров по роли
     * @param role роль пользователя
     * @return возвращает список пользователей
     */
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findAllByRole(@Param("role") Role role);
}
