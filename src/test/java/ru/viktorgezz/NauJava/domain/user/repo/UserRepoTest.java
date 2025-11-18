package ru.viktorgezz.NauJava.domain.user.repo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationPostgresTest;
import ru.viktorgezz.NauJava.domain.user.Role;
import ru.viktorgezz.NauJava.domain.user.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserRepo Integration Tests")
class UserRepoTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private UserRepo userRepo;

    private User admin1;
    private User user1;

    @BeforeEach
    void setUp() {
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

    @AfterEach
    void  tearDown() {
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("Поиск пользователя по имени")
    void findByUsername_shouldReturnUser() {
        Optional<User> found = userRepo.findByUsername(admin1.getUsername());
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(admin1.getUsername());
        assertThat(found.get().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("Возвращение пустого Optional, если пользователь не найден")
    void findByUsername_shouldReturnEmptyWhenNotFound() {
        Optional<User> found = userRepo.findByUsername("unknown");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Поиск пользователей по роли через @Query")
    void findAllByRole_shouldReturnUsersByRole() {
        List<User> admins = userRepo.findAllByRole(Role.ADMIN);
        List<User> users = userRepo.findAllByRole(Role.USER);

        assertThat(admins).hasSize(2);
        assertThat(admins).allMatch(u -> u.getRole() == Role.ADMIN);

        assertThat(users).hasSize(1);
        assertThat(users.getFirst().getUsername()).isEqualTo(user1.getUsername());
    }
}