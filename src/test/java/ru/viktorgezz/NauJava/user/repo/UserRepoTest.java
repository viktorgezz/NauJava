package ru.viktorgezz.NauJava.user.repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import ru.viktorgezz.NauJava.AbstractIntegrationPostgreTest;
import ru.viktorgezz.NauJava.user.Role;
import ru.viktorgezz.NauJava.user.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DisplayName("UserRepo Integration Tests")
class UserRepoTest extends AbstractIntegrationPostgreTest {

    @Autowired
    private UserRepo userRepo;

    private User admin1;
    private User admin2;
    private User user1;

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();

        admin1 = new User();
        admin1.setUsername("admin1");
        admin1.setPassword("pwd");
        admin1.setRole(Role.ADMIN);
        admin1 = userRepo.save(admin1);

        admin2 = new User();
        admin2.setUsername("admin2");
        admin2.setPassword("pwd");
        admin2.setRole(Role.ADMIN);
        admin2 = userRepo.save(admin2);

        user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("pwd");
        user1.setRole(Role.USER);
        user1 = userRepo.save(user1);
    }

    @Test
    @DisplayName("Должен находить пользователя по имени")
    void findByUsername_shouldReturnUser() {
        Optional<User> found = userRepo.findByUsername("admin1");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("admin1");
        assertThat(found.get().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("Должен возвращать пустой Optional, если пользователь не найден")
    void findByUsername_shouldReturnEmptyWhenNotFound() {
        Optional<User> found = userRepo.findByUsername("unknown");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Должен находить пользователей по роли через @Query")
    void findAllByRole_shouldReturnUsersByRole() {
        List<User> admins = userRepo.findAllByRole(Role.ADMIN);
        List<User> users = userRepo.findAllByRole(Role.USER);

        assertThat(admins).hasSize(2);
        assertThat(admins).allMatch(u -> u.getRole() == Role.ADMIN);

        assertThat(users).hasSize(1);
        assertThat(users.getFirst().getUsername()).isEqualTo("user1");
    }
}


