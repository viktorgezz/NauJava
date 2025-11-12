package ru.viktorgezz.NauJava.domain.report;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.viktorgezz.NauJava.AbstractIntegrationPostgresTest;
import ru.viktorgezz.NauJava.domain.report.dto.ResultResponse;
import ru.viktorgezz.NauJava.domain.report.dto.ReportUserCountResultsResponse;
import ru.viktorgezz.NauJava.domain.report.model.ReportResultData;
import ru.viktorgezz.NauJava.domain.report.model.ReportUserCountResultsModel;
import ru.viktorgezz.NauJava.domain.report.repo.UserCountResultReportRepo;
import ru.viktorgezz.NauJava.domain.report.service.intrf.ReportResultDataService;
import ru.viktorgezz.NauJava.domain.report.service.intrf.ReportService;
import ru.viktorgezz.NauJava.domain.result.Grade;
import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.result.repo.ResultRepo;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.repo.TestRepo;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;
import ru.viktorgezz.NauJava.exception.BusinessException;
import ru.viktorgezz.NauJava.exception.ErrorCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.viktorgezz.NauJava.util.CreationModel.createRandomUser;
import static ru.viktorgezz.NauJava.util.CreationModel.createResult;
import static ru.viktorgezz.NauJava.util.CreationModel.createTest;
import static ru.viktorgezz.NauJava.util.CreationModel.createUserCountResultReportModel;

@DisplayName("ReportServiceImpl Integration Tests")
class ReportServiceTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserCountResultReportRepo userCountResultReportRepo;
    @Autowired
    private ReportResultDataService reportResultDataService;

    @Autowired
    private ResultRepo resultRepo;
    @Autowired
    private TestRepo testRepo;
    @Autowired
    private UserRepo userRepo;

    private User participant;
    private TestModel testModel;
    private ReportUserCountResultsModel report;

    @BeforeEach
    void setUp() {
        User testAuthor = userRepo.save(createRandomUser());
        participant = userRepo.save(createRandomUser());

        testModel = testRepo.save(createTest(
                "Integration Test",
                "Description",
                Status.PUBLIC,
                testAuthor
        ));

        Result resultToSave = createResult(participant, Grade.A, new BigDecimal("95.5"));
        resultToSave.setTest(testModel);
        Result result = resultRepo.save(resultToSave);

        List<ResultResponse> resultResponses = new ArrayList<>(List
                .of(
                        ReportMapper.toDto(result, participant.getUsername(), testModel.getTitle())
                )
        );
        ReportResultData reportResultData = reportResultDataService.findOrCreate(resultResponses);

        report = createUserCountResultReportModel(
                StatusReport.FINISHED,
                120L,
                reportResultData,
                LocalDateTime.now().minusHours(1),
                2000L,
                2000L,
                5000L
        );

        report = userCountResultReportRepo.save(report);
    }

    @AfterEach
    void tearDown() {
        userCountResultReportRepo.deleteAll();
        resultRepo.deleteAll();
        testRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("Успешный поиск существующего отчета по ID")
    void shouldFindReportByIdSuccessfully() {
        // Arrange
        Long existingId = report.getId();

        // Act
        ReportUserCountResultsResponse response = reportService.findById(existingId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(StatusReport.FINISHED);
        assertThat(response.countUsers()).isEqualTo(120L);
        assertThat(response.timeSpentCommonMillis()).isEqualTo(5000L);

        assertThat(response.resultResponses()).isNotNull().hasSize(1);
        ResultResponse resultDto = response.resultResponses().getFirst();
        assertThat(resultDto.usernameParticipant()).isEqualTo(participant.getUsername());
        assertThat(resultDto.titleTest()).isEqualTo(testModel.getTitle());
        assertThat(resultDto.score()).isEqualByComparingTo(new BigDecimal("95.5"));
    }

    @Test
    @DisplayName("Выброс BusinessException (ErrorCode.REPORT_NOT_FOUND) при поиске несуществующего отчета")
    void shouldThrowBusinessExceptionWhenReportNotFound() {
        // Arrange
        Long nonExistentId = 9999L;
        assertThat(userCountResultReportRepo.existsById(nonExistentId)).isFalse();

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reportService.findById(nonExistentId)
        );

        // Assert
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REPORT_NOT_FOUND);
    }

    @Test
    @DisplayName("Успешная генерация отчета")
    void shouldGenerateReportSuccessfully() throws ExecutionException, InterruptedException {
        ReportUserCountResultsModel createdReport = userCountResultReportRepo.save(
                new ReportUserCountResultsModel(StatusReport.CREATED)
        );
        Long idReport = createdReport.getId();

        CompletableFuture<ReportUserCountResultsResponse> reportGeneratedFuture = reportService.generateReportAsync(idReport);

        ReportUserCountResultsResponse reportGenerated = reportGeneratedFuture.get();
        assertThat(reportGenerated).isNotNull();
        assertThat(reportGenerated.status()).isEqualTo(StatusReport.FINISHED);

        long countUserExpected = userRepo.count();
        assertThat(reportGenerated.countUsers()).isEqualTo(countUserExpected);

        long countResultExpected = resultRepo.count();
        assertThat(reportGenerated.resultResponses()).hasSize(Math.toIntExact(countResultExpected));
        assertThat(reportGenerated.resultResponses().getFirst().titleTest()).isEqualTo(testModel.getTitle());

        ReportUserCountResultsModel reportFinished = userCountResultReportRepo.findByIdWithResults(idReport).orElseThrow();
        assertThat(reportFinished.getStatus()).isEqualTo(StatusReport.FINISHED);
        assertThat(reportFinished.getCountUsers()).isEqualTo(countUserExpected);

        long countResult = reportFinished.getReportResultData().getResults().size();
        assertThat(countResult).isEqualTo(countResultExpected);
    }

    @Test
    @DisplayName("Выброс BusinessException (ErrorCode.REPORT_NOT_FOUND) при генерации несуществующего отчета")
    void shouldThrowBusinessExceptionWhenReportNotFoundForGeneration() {
        // Arrange
        Long nonExistentId = 9999L;
        assertThat(userCountResultReportRepo.existsById(nonExistentId)).isFalse();

        // Act & Assert
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reportService.generateReportAsync(nonExistentId)
        );

        // Assert
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REPORT_NOT_FOUND);
    }

    @Test
    @DisplayName("Успешное создание отчета со статусом CREATED")
    void shouldCreateReportWithCreatedStatus() {
        // Arrange
        long initialReportCount = userCountResultReportRepo.count();
        assertThat(initialReportCount).isEqualTo(1);

        // Act
        Long idNewReport = reportService.createReport();

        // Assert
        assertThat(idNewReport).isNotNull().isNotEqualTo(report.getId());

        long finalReportCount = userCountResultReportRepo.count();
        assertThat(finalReportCount).isEqualTo(initialReportCount + 1);

        ReportUserCountResultsModel createdReport = userCountResultReportRepo.findById(idNewReport)
                .orElseThrow(() -> new AssertionError("Созданный отчет не найден в БД по ID"));

        assertThat(createdReport.getStatus()).isEqualTo(StatusReport.CREATED);

        assertThat(createdReport.getCountUsers()).isNull();
        assertThat(createdReport.getTimeSpentCommonMillis()).isNull();
        assertThat(createdReport.getCompletedAt()).isNull();
        assertThat(createdReport.getReportResultData()).isNull();
    }
}
