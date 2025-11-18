package ru.viktorgezz.NauJava.domain.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;
import ru.viktorgezz.NauJava.domain.user.service.impl.UserCommandServiceImpl;
import ru.viktorgezz.NauJava.util.CreationModel;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for UserCommandServiceImpl")
class UserCommandServiceImplTest {

    @Mock
    private UserRepo repoUser;

    @InjectMocks
    private UserCommandServiceImpl serviceUserCommand;

    /**
     * <p>Тестирование успешного сохранения объекта {@code User}. Проверяется, что
     * метод сервиса корректно вызывает соответствующий метод {@code save} репозитория.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Создается валидный случайный объект {@code User} ({@code userToSave}).</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Вызывается метод {@code serviceUserCommand.save(userToSave)}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что метод {@code repoUser.save(userToSave)} был вызван ровно один раз с переданным объектом.</li>
     * </ul>
     * </ol></b>
     */
    @Test
    @DisplayName("save: успешно вызывает метод репозитория для сохранения пользователя")
    void save_ShouldCallRepoSave_WhenUserIsValid() {
        User userToSave = CreationModel.createRandomUser();

        serviceUserCommand.save(userToSave);

        verify(repoUser).save(userToSave);
    }

    /**
     * <p>Тестирование поведения метода {@code save} при передаче {@code null} в качестве аргумента.
     * Проверяется, что выбрасывается исключение {@code IllegalArgumentException},
     * имитируя стандартное поведение Spring Data Repository.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Определяется ожидаемое сообщение исключения: "Entity must not be null".</li>
     * <li>Настраивается Мок-объект {@code repoUser}: при вызове {@code save(null)} он должен выбросить {@code IllegalArgumentException} с ожидаемым сообщением.</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Вызывается метод {@code serviceUserCommand.save(null)}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что при вызове метода выбрасывается исключение типа {@code IllegalArgumentException}.</li>
     * <li>Проверить, что сообщение выброшенного исключения соответствует ожидаемому сообщению.</li>
     * <li>Проверить, что метод {@code repoUser.save(null)} был вызван ровно один раз.</li>
     * </ul>
     * </ol></b>
     */
    @Test
    @DisplayName("save: выбрасывает IllegalArgumentException, если передан null (стандартное поведение Spring Data)")
    void save_ShouldThrowException_WhenUserIsNull() {
        User userNull = null;
        String messageExpected = "Entity must not be null";

        doThrow(new IllegalArgumentException(messageExpected))
                .when(repoUser).save(userNull);

        assertThatThrownBy(() -> serviceUserCommand.save(userNull))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(messageExpected);

        verify(repoUser).save(userNull);
    }
}
