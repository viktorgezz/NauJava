package ru.viktorgezz.NauJava.result.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import ru.viktorgezz.NauJava.AbstractIntegrationPostgresTest;
import ru.viktorgezz.NauJava.result.Grade;
import ru.viktorgezz.NauJava.result.Result;
import ru.viktorgezz.NauJava.result.repo.ResultRepo;
import ru.viktorgezz.NauJava.user.User;
import ru.viktorgezz.NauJava.user.repo.UserRepo;
import ru.viktorgezz.NauJava.util.SavingModel;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DisplayName("ResultRepoImpl Criteria API Tests")
class ResultRepoImplTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private ResultRepoImpl resultRepoImpl;

    @Autowired
    private ResultRepo resultRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SavingModel savingModel;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        resultRepo.deleteAll();
        userRepo.deleteAll();

        user1 = savingModel.createAndSaveRandomUser();
        user2 = savingModel.createAndSaveRandomUser();
    }

    @Test
    @DisplayName("Поиск всех результатов по оценке и userId через Criteria API")
    void shouldFindAllByGradeAndParticipantId() {
        // Arrange
        resultRepo.save(savingModel.createResult(user1, Grade.B, new BigDecimal("82.5")));
        resultRepo.save(savingModel.createResult(user1, Grade.B, new BigDecimal("88.0")));
        resultRepo.save(savingModel.createResult(user2, Grade.B, new BigDecimal("85.0")));
        resultRepo.save(savingModel.createResult(user1, Grade.A, new BigDecimal("95.0")));

        // Act
        List<Result> results = resultRepoImpl.findAllByGradeAndUserId(Grade.B, user1.getId());

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.getGrade() == Grade.B);
        assertThat(results).allMatch(r -> r.getParticipant().getId().equals(user1.getId()));
    }

    @Test
    @DisplayName("Поиск результатов с баллами меньше указанного значения через Criteria API")
    void shouldFindWithScoreLessThan() {
        // Arrange
        resultRepo.save(savingModel.createResult(user1, Grade.F, new BigDecimal("40.0")));
        resultRepo.save(savingModel.createResult(user1, Grade.C, new BigDecimal("60.0")));
        resultRepo.save(savingModel.createResult(user1, Grade.B, new BigDecimal("80.0")));

        // Act
        List<Result> results = resultRepoImpl.findWithScoreLessThan(new BigDecimal("65.0"));

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.getScore().compareTo(new BigDecimal("65.0")) < 0);
    }

    @Test
    @DisplayName("ResultRepoImpl и ResultRepo должны возвращать одинаковые результаты по методу findAllByGradeAndUserId")
    void shouldReturnSameResultsAsRepo() {
        // Arrange
        resultRepo.save(savingModel.createResult(user1, Grade.A, new BigDecimal("95.0")));
        resultRepo.save(savingModel.createResult(user1, Grade.A, new BigDecimal("98.0")));
        resultRepo.save(savingModel.createResult(user2, Grade.A, new BigDecimal("92.0")));

        // Act
        List<Result> resultsFromRepo = resultRepo.findAllByGradeAndParticipantId(Grade.A, user1.getId());
        List<Result> resultsFromImpl = resultRepoImpl.findAllByGradeAndUserId(Grade.A, user1.getId());

        // Assert
        assertThat(resultsFromRepo).hasSize(2);
        assertThat(resultsFromImpl).hasSize(2);
        assertThat(resultsFromRepo).containsExactlyInAnyOrderElementsOf(resultsFromImpl);
    }

    @Test
    @DisplayName("Возвращение пустого списка если нет подходящих результатов")
    void shouldReturnEmptyListWhenNoMatches() {
        // Arrange
        resultRepo.save(savingModel.createResult(user1, Grade.A, new BigDecimal("95.0")));

        // Act
        List<Result> results = resultRepoImpl.findAllByGradeAndUserId(Grade.B, user1.getId());

        // Assert
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Корректная работа поиска с граничными значениями")
    void shouldHandleBoundaryValues() {
        // Arrange
        resultRepo.save(savingModel.createResult(user1, Grade.F, new BigDecimal("50.0")));
        resultRepo.save(savingModel.createResult(user1, Grade.C, new BigDecimal("49.99")));

        // Act
        List<Result> results = resultRepoImpl.findWithScoreLessThan(new BigDecimal("50.0"));

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getScore()).isEqualTo(new BigDecimal("49.99"));
    }
}