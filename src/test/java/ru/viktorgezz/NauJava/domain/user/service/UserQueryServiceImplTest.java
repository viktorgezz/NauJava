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

    @Test
    @DisplayName("getByUsername: успешно возвращает пользователя, если он найден в БД")
    void getByUsername_ShouldReturnUser_WhenExists() {
        String usernameTarget = "test_user";
        User userExpected = CreationModel.createRandomUser();
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

    @Test
    @DisplayName("findAllByRole: возвращает список пользователей с заданной ролью")
    void findAllByRole_ShouldReturnList_WhenUsersExist() {
        Role roleTarget = Role.USER;
        User userFirst = CreationModel.createRandomUser();
        User userSecond = CreationModel.createRandomUser();
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

    @Test
    @DisplayName("findAllByRole: возвращает пустой список, если пользователей с ролью нет")
    void findAllByRole_ShouldReturnEmptyList_WhenNoMatches() {
        Role roleTarget = Role.ADMIN;

        when(repoUser.findAllByRole(roleTarget)).thenReturn(Collections.emptyList());

        List<User> listActual = serviceUserQuery.findAllByRole(roleTarget);

        assertThat(listActual).isEmpty();

        verify(repoUser).findAllByRole(roleTarget);
    }

    @Test
    @DisplayName("computeCountUsers: возвращает корректное число пользователей")
    void computeCountUsers_ShouldReturnCount() {
        long countExpected = 100L;

        when(repoUser.getCountUsers()).thenReturn(countExpected);

        long countActual = serviceUserQuery.computeCountUsers();

        assertThat(countActual).isEqualTo(countExpected);

        verify(repoUser).getCountUsers();
    }

    @Test
    @DisplayName("loadUserByUsername: успешно возвращает UserDetails для Spring Security")
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        String usernameTarget = "security_user";
        User userEntity = CreationModel.createRandomUser();
        userEntity.setUsername(usernameTarget);

        when(repoUser.findByUsername(usernameTarget)).thenReturn(Optional.of(userEntity));

        UserDetails userDetailsActual = serviceUserQuery.loadUserByUsername(usernameTarget);

        assertThat(userDetailsActual).isNotNull();
        assertThat(userDetailsActual.getUsername()).isEqualTo(usernameTarget);

        verify(repoUser).findByUsername(usernameTarget);
    }

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
