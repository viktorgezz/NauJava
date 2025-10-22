package ru.viktorgezz.NauJava.user.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import ru.viktorgezz.NauJava.AbstractIntegrationPostgresTest;
import ru.viktorgezz.NauJava.user.Role;
import ru.viktorgezz.NauJava.user.User;
import ru.viktorgezz.NauJava.user.repo.UserRepo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DisplayName("UserRepoImpl Criteria API Tests")
class UserRepoImplTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private UserRepoImpl userRepoImpl;

    @Autowired
    private UserRepo userRepo;

    private User admin1;
    private User user1;

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();

        admin1 = userRepo.save(new User(
                "admin1",
                "pwd",
                Role.ADMIN
        ));

        userRepo.save(new User(
                "admin2",
                "pwd",
                Role.ADMIN
        ));

        user1 = userRepo.save(new User(
                "user1",
                "pwd",
                Role.USER
        ));
    }

    @Test
    @DisplayName("Поиск пользователя по имени через Criteria API")
    void findByUsername_Criteria_shouldReturnUser() {
        Optional<User> found = userRepoImpl.findByUsername(admin1.getUsername());
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(admin1.getUsername());
        assertThat(found.get().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("Возвращение пустого Optional, если пользователь не найден через Criteria API")
    void findByUsername_Criteria_shouldReturnEmptyWhenNotFound() {
        Optional<User> found = userRepoImpl.findByUsername("unknown");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Поиск пользователей по роли через Criteria API")
    void findAllByRole_Criteria_shouldReturnUsersByRole() {
        List<User> admins = userRepoImpl.findAllByRole(Role.ADMIN);
        List<User> users = userRepoImpl.findAllByRole(Role.USER);

        assertThat(admins).hasSize(2);
        assertThat(admins).allMatch(u -> u.getRole() == Role.ADMIN);

        assertThat(users).hasSize(1);
        assertThat(users.getFirst().getUsername()).isEqualTo(user1.getUsername());
    }
}