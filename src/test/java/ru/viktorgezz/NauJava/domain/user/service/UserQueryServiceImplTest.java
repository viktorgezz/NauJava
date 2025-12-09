package ru.viktorgezz.NauJava.domain.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.viktorgezz.NauJava.domain.user.Role;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;
import ru.viktorgezz.NauJava.domain.user.service.impl.UserQueryServiceImpl;
import ru.viktorgezz.NauJava.exception.BusinessException;
import ru.viktorgezz.NauJava.exception.ErrorCode;
import ru.viktorgezz.NauJava.util.CreationModel;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for UserQueryServiceImpl")
class UserQueryServiceImplTest {

    @Mock
    private UserRepo repoUser;

    @InjectMocks
    private UserQueryServiceImpl serviceUserQuery;

    /**
     * <p>Тестирование успешного получения объекта {@code User} по имени пользователя (username).
     * Проверяется, что сервис возвращает корректный объект, если он найден в репозитории.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Создается ожидаемый объект {@code User}.</li>
     * <li>Настраивается Мок-объект {@code repoUser}: при вызове {@code findByUsername()} возвращается {@code Optional.of(userExpected)}.</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Вызывается метод {@code serviceUserQuery.getByUsername(usernameTarget)}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что возвращенный объект не {@code null} и соответствует ожидаемому объекту.</li>
     * <li>Проверить, что у возвращенного объекта корректное имя пользователя.</li>
     * <li>Проверить, что метод {@code repoUser.findByUsername()} был вызван.</li>
     * </ul>
     * </ol></b>
     */
    @Test
    @DisplayName("getByUsername: успешно возвращает пользователя, если он найден в БД")
    void getByUsername_ShouldReturnUser_WhenExists() {
        String usernameTarget = "test_user";
        User userExpected = CreationModel.createUserRandom();
        userExpected.setUsername(usernameTarget);

        when(repoUser.findByUsername(usernameTarget)).thenReturn(Optional.of(userExpected));

        User userActual = serviceUserQuery.getByUsername(usernameTarget);

        assertThat(userActual)
                .isNotNull()
                .isEqualTo(userExpected)
                .extracting(User::getUsername)
                .isEqualTo(usernameTarget);

        verify(repoUser).findByUsername(usernameTarget);
    }

    /**
     * <p>Тестирование поведения метода {@code getByUsername} в случае, когда пользователь с
     * заданным именем не найден в репозитории. Ожидается выброс {@code BusinessException}
     * с кодом ошибки {@code USER_NOT_FOUND}.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Настраивается Мок-объект {@code repoUser}: при вызове {@code findByUsername()} возвращается {@code Optional.empty()}.</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Вызывается метод {@code serviceUserQuery.getByUsername(usernameUnknown)}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что при вызове выбрасывается исключение типа {@code BusinessException}.</li>
     * <li>Проверить, что код ошибки в исключении равен {@code ErrorCode.USER_NOT_FOUND}.</li>
     * <li>Проверить, что метод {@code repoUser.findByUsername()} был вызван.</li>
     * </ul>
     * </ol></b>
     */
    @Test
    @DisplayName("getByUsername: выбрасывает BusinessException, если пользователь не найден")
    void getByUsername_ShouldThrowBusinessException_WhenUserNotFound() {
        String usernameUnknown = "unknown_user";

        when(repoUser.findByUsername(usernameUnknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceUserQuery.getByUsername(usernameUnknown))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verify(repoUser).findByUsername(usernameUnknown);
    }

    /**
     * <p>Тестирование успешного получения списка пользователей по заданной роли.
     * Проверяется, что сервис корректно возвращает список, полученный от репозитория.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Определяется целевая роль ({@code Role.USER}).</li>
     * <li>Создается список ожидаемых объектов {@code User}, имеющих эту роль.</li>
     * <li>Настраивается Мок-объект {@code repoUser}: при вызове {@code findAllByRole(roleTarget)} возвращается ожидаемый список.</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Вызывается метод {@code serviceUserQuery.findAllByRole(roleTarget)}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что возвращенный список имеет ожидаемый размер (2).</li>
     * <li>Проверить, что список содержит именно ожидаемые объекты пользователей.</li>
     * <li>Проверить, что метод {@code repoUser.findAllByRole()} был вызван.</li>
     * </ul>
     * </ol></b>
     */
    @Test
    @DisplayName("findAllByRole: возвращает список пользователей с заданной ролью")
    void findAllByRole_ShouldReturnList_WhenUsersExist() {
        Role roleTarget = Role.USER;
        User userFirst = CreationModel.createUserRandom();
        User userSecond = CreationModel.createUserRandom();
        userFirst.setRole(roleTarget);
        userSecond.setRole(roleTarget);
        List<User> listExpected = List.of(userFirst, userSecond);

        when(repoUser.findAllByRole(roleTarget)).thenReturn(listExpected);

        List<User> listActual = serviceUserQuery.findAllByRole(roleTarget);

        assertThat(listActual)
                .hasSize(2)
                .containsExactlyInAnyOrder(userFirst, userSecond);

        verify(repoUser).findAllByRole(roleTarget);
    }

    /**
     * <p>Тестирование получения списка пользователей по роли, если в репозитории нет
     * пользователей с такой ролью. Ожидается возврат пустого списка.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Определяется целевая роль ({@code Role.ADMIN}).</li>
     * <li>Настраивается Мок-объект {@code repoUser}: при вызове {@code findAllByRole(roleTarget)} возвращается пустой список ({@code Collections.emptyList()}).</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Вызывается метод {@code serviceUserQuery.findAllByRole(roleTarget)}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что возвращенный список является пустым.</li>
     * <li>Проверить, что метод {@code repoUser.findAllByRole()} был вызван.</li>
     * </ul>
     * </ol></b>
     */
    @Test
    @DisplayName("findAllByRole: возвращает пустой список, если пользователей с ролью нет")
    void findAllByRole_ShouldReturnEmptyList_WhenNoMatches() {
        Role roleTarget = Role.ADMIN;

        when(repoUser.findAllByRole(roleTarget)).thenReturn(Collections.emptyList());

        List<User> listActual = serviceUserQuery.findAllByRole(roleTarget);

        assertThat(listActual).isEmpty();

        verify(repoUser).findAllByRole(roleTarget);
    }

    /**
     * <p>Тестирование метода подсчета общего числа пользователей. Проверяется, что
     * сервис корректно возвращает значение, полученное от репозитория.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Определяется ожидаемое количество пользователей ({@code 100L}).</li>
     * <li>Настраивается Мок-объект {@code repoUser}: при вызове {@code getCountUsers()} возвращается ожидаемое число.</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Вызывается метод {@code serviceUserQuery.computeCountUsers()}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что возвращенное значение ({@code long countActual}) равно ожидаемому.</li>
     * <li>Проверить, что метод {@code repoUser.getCountUsers()} был вызван.</li>
     * </ul>
     * </ol></b>
     */
    @Test
    @DisplayName("computeCountUsers: возвращает корректное число пользователей")
    void computeCountUsers_ShouldReturnCount() {
        long countExpected = 100L;

        when(repoUser.getCountUsers()).thenReturn(countExpected);

        long countActual = serviceUserQuery.computeCountUsers();

        assertThat(countActual).isEqualTo(countExpected);

        verify(repoUser).getCountUsers();
    }

    /**
     * <p>Тестирование успешной реализации метода {@code loadUserByUsername} из {@code UserDetailsService}
     * для Spring Security. Проверяется, что при наличии пользователя возвращается корректный объект {@code UserDetails}.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Создается объект {@code User} для имитации найденной сущности.</li>
     * <li>Настраивается Мок-объект {@code repoUser}: при вызове {@code findByUsername()} возвращается {@code Optional.of(userEntity)}.</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Вызывается метод {@code serviceUserQuery.loadUserByUsername(usernameTarget)}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что возвращенный объект не {@code null}.</li>
     * <li>Проверить, что имя пользователя в {@code UserDetails} соответствует ожидаемому.</li>
     * <li>Проверить, что метод {@code repoUser.findByUsername()} был вызван.</li>
     * </ul>
     * </ol></b>
     */
    @Test
    @DisplayName("loadUserByUsername: успешно возвращает UserDetails для Spring Security")
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        String usernameTarget = "security_user";
        User userEntity = CreationModel.createUserRandom();
        userEntity.setUsername(usernameTarget);

        when(repoUser.findByUsername(usernameTarget)).thenReturn(Optional.of(userEntity));

        UserDetails userDetailsActual = serviceUserQuery.loadUserByUsername(usernameTarget);

        assertThat(userDetailsActual).isNotNull();
        assertThat(userDetailsActual.getUsername()).isEqualTo(usernameTarget);

        verify(repoUser).findByUsername(usernameTarget);
    }

    /**
     * <p>Тестирование реализации метода {@code loadUserByUsername} при отсутствии пользователя.
     * Проверяется, что в этом случае выбрасывается стандартное исключение Spring Security
     * {@code UsernameNotFoundException}.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Определяется отсутствующее имя пользователя ({@code usernameMissing}).</li>
     * <li>Настраивается Мок-объект {@code repoUser}: при вызове {@code findByUsername()} возвращается {@code Optional.empty()}.</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Вызывается метод {@code serviceUserQuery.loadUserByUsername(usernameMissing)}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что при вызове выбрасывается исключение типа {@code UsernameNotFoundException}.</li>
     * <li>Проверить, что сообщение исключения содержит имя пользователя, которое не было найдено.</li>
     * <li>Проверить, что метод {@code repoUser.findByUsername()} был вызван.</li>
     * <li>Проверить, что других взаимодействий с моком {@code repoUser} не было.</li>
     * </ul>
     * </ol></b>
     */
    @Test
    @DisplayName("loadUserByUsername: выбрасывает UsernameNotFoundException, если пользователь не найден")
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        String usernameMissing = "missing_person";

        when(repoUser.findByUsername(usernameMissing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceUserQuery.loadUserByUsername(usernameMissing))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining(usernameMissing);

        verify(repoUser).findByUsername(usernameMissing);
        verifyNoMoreInteractions(repoUser);
    }

}
