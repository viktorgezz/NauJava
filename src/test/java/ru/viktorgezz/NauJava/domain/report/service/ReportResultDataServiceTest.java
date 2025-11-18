package ru.viktorgezz.NauJava.domain.report.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationPostgresTest;
import ru.viktorgezz.NauJava.domain.report.dto.ResultResponse;
import ru.viktorgezz.NauJava.domain.report.model.ReportResultData;
import ru.viktorgezz.NauJava.domain.report.repo.ReportResultDataRepo;
import ru.viktorgezz.NauJava.domain.report.service.intrf.ReportResultDataService;
import ru.viktorgezz.NauJava.domain.result.Grade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.viktorgezz.NauJava.util.CreationModel.createResultResponse;

@DisplayName("ReportResultDataServiceImpl Integration Tests")
class ReportResultDataServiceTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private ReportResultDataService reportResultDataService;

    @Autowired
    private ReportResultDataRepo reportResultDataRepo;

    private List<ResultResponse> resultsA;
    private List<ResultResponse> resultsB;

    @BeforeEach
    void setUp() {
        resultsA = List.of(
                createResultResponse(new BigDecimal("95.5"), Grade.A, "user1", "Java"),
                createResultResponse(new BigDecimal("80.0"), Grade.B, "user2", "c++")
        );

        resultsB = List.of(
                createResultResponse(new BigDecimal("79.0"), Grade.B, "user1", "Python")
        );
    }

    @AfterEach
    void tearDown() {
        reportResultDataRepo.deleteAll();
    }

    @Test
    @DisplayName("Успешное создание новой записи, если хэш не найден")
    void shouldCreateNewDataWhenHashNotFound() {
        // Arrange
        assertThat(reportResultDataRepo.count()).isZero();

        // Act
        ReportResultData reportDataCreation = reportResultDataService.findOrCreate(resultsA);

        // Assert
        assertThat(reportDataCreation).isNotNull();
        assertThat(reportDataCreation.getId()).isNotNull();
        assertThat(reportDataCreation.getResults()).isEqualTo(resultsA);
        assertThat(reportDataCreation.getResultsHash()).isNotNull();

        assertThat(reportResultDataRepo.count()).isEqualTo(1);
        ReportResultData savedData = reportResultDataRepo.findById(reportDataCreation.getId()).orElseThrow();
        assertThat(savedData.getResults()).hasSize(2);
        assertThat(savedData.getResults().getFirst().usernameParticipant()).isEqualTo("user1");
    }

    @Test
    @DisplayName("Успешный поиск существующей записи, если хэш найден")
    void shouldFindExistingDataWhenHashFound() {
        // Arrange
        ReportResultData initialData = reportResultDataService.findOrCreate(resultsA);
        assertThat(reportResultDataRepo.count()).isEqualTo(1);

        // Act
        ReportResultData reportResultFound = reportResultDataService.findOrCreate(resultsA);

        // Assert
        assertThat(reportResultFound).isNotNull();
        assertThat(reportResultFound.getId()).isEqualTo(initialData.getId());
        assertThat(reportResultFound.getResultsHash()).isEqualTo(initialData.getResultsHash());
        assertThat(reportResultDataRepo.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Недопущение дублирования при повторном вызове с идентичными данными")
    void shouldNotCreateDuplicateOnIdenticalCall() {
        // Arrange
        List<ResultResponse> resultsAIdentical = new ArrayList<>(resultsA);

        assertThat(reportResultDataRepo.count()).isZero();

        // Act
        ReportResultData reportResult1 = reportResultDataService.findOrCreate(resultsA);
        ReportResultData reportResult2 = reportResultDataService.findOrCreate(resultsAIdentical);

        // Assert
        assertThat(reportResult1).isNotNull();
        assertThat(reportResult2).isNotNull();
        assertThat(reportResult1.getId()).isEqualTo(reportResult2.getId());
        assertThat(reportResultDataRepo.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Корректное создание разных записей для разных данных")
    void shouldCreateSeparateRecordsForDifferentData() {
        // Arrange
        assertThat(reportResultDataRepo.count()).isZero();

        // Act
        ReportResultData reportResult1 = reportResultDataService.findOrCreate(resultsA);
        ReportResultData reportResult2 = reportResultDataService.findOrCreate(resultsB);

        // Assert
        assertThat(reportResult1).isNotNull();
        assertThat(reportResult2).isNotNull();
        assertThat(reportResult1.getId()).isNotEqualTo(reportResult2.getId());
        assertThat(reportResult1.getResultsHash()).isNotEqualTo(reportResult2.getResultsHash());
        assertThat(reportResultDataRepo.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Успешное создание записи для пустого списка")
    void shouldCreateRecordForEmptyList() {
        // Arrange
        List<ResultResponse> emptyList = new ArrayList<>();
        String emptyListHash = DigestUtils.sha256Hex("[]");
        assertThat(reportResultDataRepo.count()).isZero();

        // Act
        ReportResultData reportResult = reportResultDataService.findOrCreate(emptyList);

        // Assert
        assertThat(reportResult).isNotNull();
        assertThat(reportResult.getId()).isNotNull();
        assertThat(reportResult.getResults()).isEmpty();
        assertThat(reportResult.getResultsHash()).isEqualTo(emptyListHash);
        assertThat(reportResultDataRepo.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Выброс RuntimeException при передаче null")
    void shouldThrowRuntimeExceptionWhenInputIsNull() {
        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> reportResultDataService.findOrCreate(null)
        );

        // Assert
        assertThat(exception.getMessage()).isEqualTo("results is null");
    }
}
