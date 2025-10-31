package ru.viktorgezz.NauJava.result.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.viktorgezz.NauJava.AbstractIntegrationPostgresTest;
import ru.viktorgezz.NauJava.result.Grade;
import ru.viktorgezz.NauJava.result.Result;
import ru.viktorgezz.NauJava.result.repo.ResultRepo;
import ru.viktorgezz.NauJava.result.service.intrf.ResultCommandService;
import ru.viktorgezz.NauJava.user.User;
import ru.viktorgezz.NauJava.user.repo.UserRepo;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.viktorgezz.NauJava.util.CreationModel.createRandomUser;
import static ru.viktorgezz.NauJava.util.CreationModel.createResult;

@DisplayName("ResultServiceImpl Integration Tests")
class ResultCommandServiceImplTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private ResultCommandService resultCommandService;

    @Autowired
    private ResultRepo resultRepo;

    @Autowired
    private UserRepo userRepo;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepo.save(createRandomUser());
    }

    @AfterEach
    void tearDown() {
        resultRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("Удаление существующего Result")
    void shouldDeleteExistingResultSuccessfully() {
        // Arrange
        Result resultToSave = createResult(testUser, Grade.B, new BigDecimal("88.0"));
        Result savedResult = resultRepo.save(resultToSave);
        Long resultId = savedResult.getId();

        assertThat(resultRepo.existsById(resultId)).isTrue();
        // Act
        resultCommandService.deleteResult(resultId);

        // Assert
        assertThat(resultRepo.existsById(resultId)).isFalse();
    }

    @Test
    @DisplayName("Выброс EntityNotFoundException при попытке удалить несуществующий Result")
    void shouldThrowExceptionWhenDeletingNonExistentResult() {
        // Arrange
        Long nonExistentResultId = 999L;
        assertThat(resultRepo.existsById(nonExistentResultId)).isFalse();

        Result existingResult = resultRepo.save(createResult(testUser, Grade.C, new BigDecimal("75.0")));
        Long existingResultId = existingResult.getId();


        // Assert
        assertThrows(
                EntityNotFoundException.class,
                () -> resultCommandService.deleteResult(nonExistentResultId)
        );
        assertThat(resultRepo.existsById(existingResultId)).isTrue();
    }
}