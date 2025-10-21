package ru.viktorgezz.NauJava.result.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import ru.viktorgezz.NauJava.AbstractIntegrationPostgresTest;
import ru.viktorgezz.NauJava.result.Grade;
import ru.viktorgezz.NauJava.result.Result;
import ru.viktorgezz.NauJava.result.repo.ResultRepo;
import ru.viktorgezz.NauJava.result.service.intrf.ResultService;
import ru.viktorgezz.NauJava.user.User;
import ru.viktorgezz.NauJava.user.repo.UserRepo;
import ru.viktorgezz.NauJava.util.SavingModel;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@DisplayName("ResultServiceImpl Integration Tests")
class ResultServiceImplTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private ResultService resultService;

    @Autowired
    private ResultRepo resultRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SavingModel savingModel;

    private User testUser;

    @BeforeEach
    void setUp() {
        resultRepo.deleteAll();
        userRepo.deleteAll();

        testUser = savingModel.createAndSaveRandomUser();
    }

    @Test
    @DisplayName("Удаление существующего Result")
    void shouldDeleteExistingResultSuccessfully() {
        // Arrange
        Result resultToSave = savingModel.createResult(testUser, Grade.B, new BigDecimal("88.0"));
        Result savedResult = resultRepo.save(resultToSave);
        Long resultId = savedResult.getId();

        assertThat(resultRepo.existsById(resultId)).isTrue();
        // Act
        resultService.deleteResult(resultId);

        // Assert
        assertThat(resultRepo.existsById(resultId)).isFalse();
    }

    @Test
    @DisplayName("Выброс EntityNotFoundException при попытке удалить несуществующий Result")
    void shouldThrowExceptionWhenDeletingNonExistentResult() {
        // Arrange
        Long nonExistentResultId = 999L;
        assertThat(resultRepo.existsById(nonExistentResultId)).isFalse();

        Result existingResult = resultRepo.save(savingModel.createResult(testUser, Grade.C, new BigDecimal("75.0")));
        Long existingResultId = existingResult.getId();


        // Assert
        assertThrows(
                EntityNotFoundException.class,
                () -> resultService.deleteResult(nonExistentResultId)
        );
        assertThat(resultRepo.existsById(existingResultId)).isTrue();
    }
}