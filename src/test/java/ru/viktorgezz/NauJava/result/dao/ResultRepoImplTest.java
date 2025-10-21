package ru.viktorgezz.NauJava.result.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import ru.viktorgezz.NauJava.AbstractIntegrationPostgreTest;
import ru.viktorgezz.NauJava.result.Grade;
import ru.viktorgezz.NauJava.result.Result;
import ru.viktorgezz.NauJava.result.repo.ResultRepo;
import ru.viktorgezz.NauJava.user.Role;
import ru.viktorgezz.NauJava.user.User;
import ru.viktorgezz.NauJava.user.repo.UserRepo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DisplayName("ResultRepoImpl Criteria API Tests")
class ResultRepoImplTest extends AbstractIntegrationPostgreTest {

    @Autowired
    private ResultRepoImpl resultRepoImpl;

    @Autowired
    private ResultRepo resultRepo;

    @Autowired
    private UserRepo userRepo;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        resultRepo.deleteAll();
        userRepo.deleteAll();

        user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("password1");
        user1.setRole(Role.USER);
        user1 = userRepo.save(user1);

        user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password2");
        user2.setRole(Role.USER);
        user2 = userRepo.save(user2);
    }

    @Test
    @DisplayName("Должен найти все результаты по оценке и userId через Criteria API")
    void shouldFindAllByGradeAndUserId() {
        // Arrange
        resultRepo.save(createResult(user1, Grade.B, new BigDecimal("82.5")));
        resultRepo.save(createResult(user1, Grade.B, new BigDecimal("88.0")));
        resultRepo.save(createResult(user2, Grade.B, new BigDecimal("85.0")));
        resultRepo.save(createResult(user1, Grade.A, new BigDecimal("95.0")));

        // Act
        List<Result> results = resultRepoImpl.findAllByGradeAndUserId(Grade.B, user1.getId());

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.getGrade() == Grade.B);
        assertThat(results).allMatch(r -> r.getUser().getId().equals(user1.getId()));
    }

    @Test
    @DisplayName("Должен найти результаты с баллами меньше указанного значения через Criteria API")
    void shouldFindWithScoreLessThan() {
        // Arrange
        resultRepo.save(createResult(user1, Grade.F, new BigDecimal("40.0")));
        resultRepo.save(createResult(user1, Grade.C, new BigDecimal("60.0")));
        resultRepo.save(createResult(user1, Grade.B, new BigDecimal("80.0")));

        // Act
        List<Result> results = resultRepoImpl.findWithScoreLessThan(new BigDecimal("65.0"));

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.getScore().compareTo(new BigDecimal("65.0")) < 0);
    }

    @Test
    @DisplayName("ResultRepoImpl и ResultRepo должны возвращать одинаковые результаты")
    void shouldReturnSameResultsAsRepo() {
        // Arrange
        resultRepo.save(createResult(user1, Grade.A, new BigDecimal("95.0")));
        resultRepo.save(createResult(user1, Grade.A, new BigDecimal("98.0")));
        resultRepo.save(createResult(user2, Grade.A, new BigDecimal("92.0")));

        // Act
        List<Result> resultsFromRepo = resultRepo.findAllByGradeAndUserId(Grade.A, user1.getId());
        List<Result> resultsFromImpl = resultRepoImpl.findAllByGradeAndUserId(Grade.A, user1.getId());

        // Assert
        assertThat(resultsFromRepo).hasSize(2);
        assertThat(resultsFromImpl).hasSize(2);
        assertThat(resultsFromRepo).containsExactlyInAnyOrderElementsOf(resultsFromImpl);
    }

    @Test
    @DisplayName("Должен вернуть пустой список если нет подходящих результатов")
    void shouldReturnEmptyListWhenNoMatches() {
        // Arrange
        resultRepo.save(createResult(user1, Grade.A, new BigDecimal("95.0")));

        // Act
        List<Result> results = resultRepoImpl.findAllByGradeAndUserId(Grade.B, user1.getId());

        // Assert
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Должен корректно работать с граничными значениями")
    void shouldHandleBoundaryValues() {
        // Arrange
        resultRepo.save(createResult(user1, Grade.F, new BigDecimal("50.0")));
        resultRepo.save(createResult(user1, Grade.C, new BigDecimal("49.99")));

        // Act
        List<Result> results = resultRepoImpl.findWithScoreLessThan(new BigDecimal("50.0"));

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getScore()).isEqualTo(new BigDecimal("49.99"));
    }

    private Result createResult(User user, Grade grade, BigDecimal score) {
        Result result = new Result();
        result.setUser(user);
        result.setGrade(grade);
        result.setScore(score);
        result.setCompletedAt(LocalDateTime.now());
        return result;
    }
}