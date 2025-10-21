package ru.viktorgezz.NauJava.result.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import ru.viktorgezz.NauJava.AbstractIntegrationPostgreTest;
import ru.viktorgezz.NauJava.result.Grade;
import ru.viktorgezz.NauJava.result.Result;
import ru.viktorgezz.NauJava.result.repo.ResultRepo;
import ru.viktorgezz.NauJava.result.service.intrf.ResultService;
import ru.viktorgezz.NauJava.user.Role;
import ru.viktorgezz.NauJava.user.User;
import ru.viktorgezz.NauJava.user.repo.UserRepo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@DisplayName("ResultServiceImpl Integration Tests")
class ResultServiceImplTest extends AbstractIntegrationPostgreTest {

    @Autowired
    private ResultService resultService;

    @Autowired
    private ResultRepo resultRepo;

    @Autowired
    private UserRepo userRepo;


    private User testUser;

    @BeforeEach
    void setUp() {
        resultRepo.deleteAll();
        userRepo.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setRole(Role.USER);
        testUser = userRepo.save(testUser);

    }

    @Test
    @DisplayName("Должен успешно удалить существующий Result")
    void shouldDeleteExistingResultSuccessfully() {
        // Arrange
        Result resultToSave = createResult(testUser, Grade.B, new BigDecimal("88.0"));
        Result savedResult = resultRepo.save(resultToSave);
        Long resultId = savedResult.getId();

        assertThat(resultRepo.existsById(resultId)).isTrue();
        // Act
        resultService.deleteResult(resultId);

        // Assert
        assertThat(resultRepo.existsById(resultId)).isFalse();
    }

    @Test
    @DisplayName("Должен выбросить EntityNotFoundException при попытке удалить несуществующий Result")
    void shouldThrowExceptionWhenDeletingNonExistentResult() {
        // Arrange
        Long nonExistentResultId = 999L;
        assertThat(resultRepo.existsById(nonExistentResultId)).isFalse();

        Result existingResult = resultRepo.save(createResult(testUser, Grade.C, new BigDecimal("75.0")));
        Long existingResultId = existingResult.getId();


        // Assert
        assertThrows(
                EntityNotFoundException.class,
                () -> resultService.deleteResult(nonExistentResultId)
        );
        assertThat(resultRepo.existsById(existingResultId)).isTrue();
    }

    private Result createResult(User user, Grade grade, BigDecimal score) {
        Result result = new Result();
        result.setUser(user);
        result.setGrade(grade);
        result.setScore(score);
        result.setCompletedAt(LocalDateTime.now());
        result.setTimeSpentSeconds(120);
        return result;
    }
}