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

    @Test
    @DisplayName("save: успешно вызывает метод репозитория для сохранения пользователя")
    void save_ShouldCallRepoSave_WhenUserIsValid() {
        User userToSave = CreationModel.createRandomUser();

        serviceUserCommand.save(userToSave);

        verify(repoUser).save(userToSave);
    }

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
