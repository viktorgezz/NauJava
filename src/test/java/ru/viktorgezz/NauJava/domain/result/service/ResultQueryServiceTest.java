package ru.viktorgezz.NauJava.domain.result.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.viktorgezz.NauJava.AbstractIntegrationPostgresTest;
import ru.viktorgezz.NauJava.domain.result.Grade;
import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.result.repo.ResultRepo;
import ru.viktorgezz.NauJava.domain.result.service.intrf.ResultQueryService;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.viktorgezz.NauJava.util.CreationModel.createRandomUser;
import static ru.viktorgezz.NauJava.util.CreationModel.createResult;

@DisplayName("ResultQueryServiceImpl Integration Tests")
class ResultQueryServiceTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private ResultQueryService resultQueryService;

    @Autowired
    private ResultRepo resultRepo;

    @Autowired
    private UserRepo userRepo;

    private User user1;
    private Result result1; // User1, Grade A, 95.0
    private Result result2; // User1, Grade B, 85.0

    @BeforeEach
    void setUp() {
        user1 = userRepo.save(createRandomUser());
        User user2 = userRepo.save(createRandomUser());

        result1 = resultRepo.save(createResult(user1, Grade.A, new BigDecimal("95.0")));
        result2 = resultRepo.save(createResult(user1, Grade.B, new BigDecimal("85.0")));
        resultRepo.save(createResult(user2, Grade.A, new BigDecimal("92.0")));
    }

    @AfterEach
    void tearDown() {
        resultRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("Должен найти все результаты")
    void shouldFindAllResults() {
        // Act
        List<Result> results = resultQueryService.findAll();

        // Assert
        assertThat(results).hasSize(3);
    }

    @Test
    @DisplayName("Должен найти результаты с баллами < 90")
    void shouldFindWithScoreLessThan() {
        BigDecimal scoreLimit = new BigDecimal("90.0");

        // Act
        List<Result> results = resultQueryService.findWithScoreLessThan(scoreLimit);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getId()).isEqualTo(result2.getId());
        assertThat(results.getFirst().getScore()).isEqualByComparingTo(new BigDecimal("85.0"));
    }

    @Test
    @DisplayName("Должен найти результаты по Оценке и ID пользователя")
    void shouldFindAllByGradeAndParticipantId() {
        // Act
        List<Result> results = resultQueryService.findAllByGradeAndParticipantId(Grade.A, user1.getId());

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getId()).isEqualTo(result1.getId());
        assertThat(results.getFirst().getParticipant().getId()).isEqualTo(user1.getId());
        assertThat(results.getFirst().getGrade()).isEqualTo(Grade.A);
    }
}
