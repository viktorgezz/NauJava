package ru.viktorgezz.NauJava.user.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import ru.viktorgezz.NauJava.AbstractIntegrationPostgreTest;
import ru.viktorgezz.NauJava.user.Role;
import ru.viktorgezz.NauJava.user.User;
import ru.viktorgezz.NauJava.user.repo.UserRepo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DisplayName("UserRepoImpl Criteria API Tests")
class UserRepoImplTest extends AbstractIntegrationPostgreTest {

    @Autowired
    private UserRepoImpl userRepoImpl;

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
    @DisplayName("Должен находить пользователя по имени через Criteria API")
    void findByUsername_Criteria_shouldReturnUser() {
        Optional<User> found = userRepoImpl.findByUsername("admin1");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("admin1");
        assertThat(found.get().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("Должен возвращать пустой Optional, если пользователь не найден (Criteria API)")
    void findByUsername_Criteria_shouldReturnEmptyWhenNotFound() {
        Optional<User> found = userRepoImpl.findByUsername("unknown");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Должен находить пользователей по роли через Criteria API")
    void findAllByRole_Criteria_shouldReturnUsersByRole() {
        List<User> admins = userRepoImpl.findAllByRole(Role.ADMIN);
        List<User> users = userRepoImpl.findAllByRole(Role.USER);

        assertThat(admins).hasSize(2);
        assertThat(admins).allMatch(u -> u.getRole() == Role.ADMIN);

        assertThat(users).hasSize(1);
        assertThat(users.getFirst().getUsername()).isEqualTo("user1");
    }
}


