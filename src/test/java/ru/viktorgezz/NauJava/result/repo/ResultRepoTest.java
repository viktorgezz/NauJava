package ru.viktorgezz.NauJava.result.repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import ru.viktorgezz.NauJava.AbstractIntegrationPostgresTest;
import ru.viktorgezz.NauJava.result.Grade;
import ru.viktorgezz.NauJava.result.Result;
import ru.viktorgezz.NauJava.user.User;
import ru.viktorgezz.NauJava.user.repo.UserRepo;
import ru.viktorgezz.NauJava.util.GeneratorRandomModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DisplayName("ResultRepo Integration Tests")
class ResultRepoTest extends AbstractIntegrationPostgresTest {

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

        user1 = createAndSaveRandomUser();
        user2 = createAndSaveRandomUser();
    }

    @Test
    @DisplayName("Поиск всех результаты по оценке и userId")
    void resultRepo_shouldFindAllByGradeAndUserId() {
        // Arrange
        resultRepo.save(createResult(user1, Grade.A, new BigDecimal("95.5")));
        resultRepo.save(createResult(user1, Grade.A, new BigDecimal("98.0")));
        resultRepo.save(createResult(user1, Grade.B, new BigDecimal("85.0")));
        resultRepo.save(createResult(user2, Grade.A, new BigDecimal("92.0")));

        // Act
        List<Result> results = resultRepo.findAllByGradeAndUserId(Grade.A, user1.getId());

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.getGrade() == Grade.A);
        assertThat(results).allMatch(r -> r.getParticipant().getId().equals(user1.getId()));
    }

    @Test
    @DisplayName("Возвращение пустого списка если нет результатов с указанной оценкой")
    void resultRepo_shouldReturnEmptyListWhenNoResultsWithGrade() {
        // Arrange
        resultRepo.save(createResult(user1, Grade.B, new BigDecimal("85.0")));

        // Act
        List<Result> results = resultRepo.findAllByGradeAndUserId(Grade.A, user1.getId());

        // Assert
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Поиск результатов с баллами меньше указанного значения")
    void resultRepo_shouldFindWithScoreLessThan() {
        // Arrange
        resultRepo.save(createResult(user1, Grade.F, new BigDecimal("45.5")));
        resultRepo.save(createResult(user1, Grade.C, new BigDecimal("65.0")));
        resultRepo.save(createResult(user1, Grade.C, new BigDecimal("75.0")));
        resultRepo.save(createResult(user2, Grade.A, new BigDecimal("95.0")));

        // Act
        List<Result> results = resultRepo.findWithScoreLessThan(new BigDecimal("70.0"));

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.getScore().compareTo(new BigDecimal("70.0")) < 0);
        assertThat(results).extracting(Result::getScore)
                .containsExactlyInAnyOrder(new BigDecimal("45.50"), new BigDecimal("65.00"));
    }

    @Test
    @DisplayName("Поиск результатов с баллом равным граничному значению")
    void resultRepo_shouldNotIncludeScoreEqualToMaxScore() {
        // Arrange
        resultRepo.save(createResult(user1, Grade.B, new BigDecimal("70.0")));
        resultRepo.save(createResult(user1, Grade.C, new BigDecimal("69.9")));

        // Act
        List<Result> results = resultRepo.findWithScoreLessThan(new BigDecimal("70.0"));

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getScore()).isEqualTo(new BigDecimal("69.90"));
    }

    @Test
    @DisplayName("Корректная работа с граничными значениями баллов")
    void shouldHandleBoundaryScoreValues() {
        // Arrange
        resultRepo.save(createResult(user1, Grade.F, new BigDecimal("0.00")));
        resultRepo.save(createResult(user1, Grade.A, new BigDecimal("100.0")));
        resultRepo.save(createResult(user1, Grade.C, new BigDecimal("50.0")));

        // Act
        List<Result> results = resultRepo.findWithScoreLessThan(new BigDecimal("50.0"));

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getScore()).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    @DisplayName("Корректная работа со всеми типами оценок")
    void shouldWorkWithAllGradeTypes() {
        // Arrange
        resultRepo.save(createResult(user1, Grade.A, new BigDecimal("95.0")));
        resultRepo.save(createResult(user1, Grade.B, new BigDecimal("85.0")));
        resultRepo.save(createResult(user1, Grade.C, new BigDecimal("65.0")));
        resultRepo.save(createResult(user1, Grade.F, new BigDecimal("45.0")));

        // Act & Assert
        assertThat(resultRepo.findAllByGradeAndUserId(Grade.A, user1.getId())).hasSize(1);
        assertThat(resultRepo.findAllByGradeAndUserId(Grade.B, user1.getId())).hasSize(1);
        assertThat(resultRepo.findAllByGradeAndUserId(Grade.C, user1.getId())).hasSize(1);
        assertThat(resultRepo.findAllByGradeAndUserId(Grade.F, user1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Корректное обрабатывание несколько пользователей")
    void shouldHandleMultipleUsers() {
        // Arrange
        resultRepo.save(createResult(user1, Grade.A, new BigDecimal("95.0")));
        resultRepo.save(createResult(user1, Grade.B, new BigDecimal("85.0")));
        resultRepo.save(createResult(user2, Grade.A, new BigDecimal("90.0")));
        resultRepo.save(createResult(user2, Grade.A, new BigDecimal("92.0")));

        // Act
        List<Result> user1Results = resultRepo.findAllByGradeAndUserId(Grade.A, user1.getId());
        List<Result> user2Results = resultRepo.findAllByGradeAndUserId(Grade.A, user2.getId());

        // Assert
        assertThat(user1Results).hasSize(1);
        assertThat(user2Results).hasSize(2);
        assertThat(user1Results).allMatch(r -> r.getParticipant().getId().equals(user1.getId()));
        assertThat(user2Results).allMatch(r -> r.getParticipant().getId().equals(user2.getId()));
    }

    @Test
    @DisplayName("Корректное нахождение результаты с десятичными баллами")
    void shouldHandleDecimalScores() {
        // Arrange
        resultRepo.save(createResult(user1, Grade.B, new BigDecimal("75.5")));
        resultRepo.save(createResult(user1, Grade.B, new BigDecimal("75.49")));
        resultRepo.save(createResult(user1, Grade.B, new BigDecimal("75.51")));

        // Act
        List<Result> results = resultRepo.findWithScoreLessThan(new BigDecimal("75.5"));

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getScore()).isEqualTo(new BigDecimal("75.49"));
    }

    @Test
    @DisplayName("Возвращение пустого списка когда нет результатов меньше порога")
    void shouldReturnEmptyWhenNoResultsBelowThreshold() {
        // Arrange
        resultRepo.save(createResult(user1, Grade.A, new BigDecimal("95.0")));
        resultRepo.save(createResult(user1, Grade.A, new BigDecimal("98.0")));

        // Act
        List<Result> results = resultRepo.findWithScoreLessThan(new BigDecimal("90.0"));

        // Assert
        assertThat(results).isEmpty();
    }

    private Result createResult(
            User user,
            Grade grade,
            BigDecimal score
    ) {
        Result result = new Result();
        result.setParticipant(user);
        result.setGrade(grade);
        result.setScore(score);
        result.setCompletedAt(LocalDateTime.now());
        return result;
    }

    private User createAndSaveRandomUser() {
        return userRepo.save(GeneratorRandomModel.getRandomUser());
    }
}