package ru.viktorgezz.NauJava.domain.report;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.viktorgezz.NauJava.AbstractIntegrationPostgresTest;
import ru.viktorgezz.NauJava.domain.many_to_many_entity.user_count_result_report.UserResultReport;
import ru.viktorgezz.NauJava.domain.many_to_many_entity.user_count_result_report.UserResultReportRepo;
import ru.viktorgezz.NauJava.domain.report.dto.ResultResponse;
import ru.viktorgezz.NauJava.domain.report.dto.ReportUserCountResultsResponse;
import ru.viktorgezz.NauJava.domain.report.service.ReportService;
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
    private UserResultReportRepo userResultReportRepo;
    @Autowired
    private ResultRepo resultRepo;
    @Autowired
    private TestRepo testRepo;
    @Autowired
    private UserRepo userRepo;

    private User participant;
    private TestModel testModel;
    private Result result;
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
        result = resultRepo.save(resultToSave);

        report =  createUserCountResultReportModel(
                StatusReport.FINISHED,
                120L,
                LocalDateTime.now().minusHours(1),
                2000L,
                2000L,
                5000L
        );
        report = userCountResultReportRepo.save(report);
        linkAndSaveReport(report, result);
    }

    @AfterEach
    void tearDown() {
        userResultReportRepo.deleteAll();
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
        Long reportId = createdReport.getId();

        CompletableFuture<ReportUserCountResultsResponse> reportGeneratedFuture = reportService.generationReport(reportId);

        ReportUserCountResultsResponse reportGenerated = reportGeneratedFuture.get();
        assertThat(reportGenerated).isNotNull();
        assertThat(reportGenerated.status()).isEqualTo(StatusReport.FINISHED);

        long expectedUserCount = userRepo.count();
        assertThat(reportGenerated.countUsers()).isEqualTo(expectedUserCount);

        long expectedResultCount = resultRepo.count();
        assertThat(reportGenerated.resultResponses()).hasSize(Math.toIntExact(expectedResultCount));
        assertThat(reportGenerated.resultResponses().getFirst().titleTest()).isEqualTo(testModel.getTitle());

        ReportUserCountResultsModel reportFinished = userCountResultReportRepo.findById(reportId).orElseThrow();
        assertThat(reportFinished.getStatus()).isEqualTo(StatusReport.FINISHED);
        assertThat(reportFinished.getCountUsers()).isEqualTo(expectedUserCount);

        List<UserResultReport> links = userResultReportRepo.findAll();
        long reportLinks = links.stream()
                .filter(it -> it.getUserCountResultReportModel().getId().equals(reportId))
                .count();
        assertThat(reportLinks).isEqualTo(expectedResultCount);
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
                () -> reportService.generationReport(nonExistentId)
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
        assertThat(idNewReport).isNotNull();
        assertThat(idNewReport).isNotEqualTo(report.getId());

        long finalReportCount = userCountResultReportRepo.count();
        assertThat(finalReportCount).isEqualTo(initialReportCount + 1);

        ReportUserCountResultsModel createdReport = userCountResultReportRepo.findById(idNewReport)
                .orElseThrow(() -> new AssertionError("Созданный отчет не найден в БД по ID"));

        assertThat(createdReport.getStatus()).isEqualTo(StatusReport.CREATED);

        assertThat(createdReport.getCountUsers()).isNull();
        assertThat(createdReport.getTimeSpentCommonMillis()).isNull();
        assertThat(createdReport.getCompletedAt()).isNull();
        assertThat(createdReport.getUserResultReports()).isEmpty();
    }


    private void linkAndSaveReport(ReportUserCountResultsModel report, Result result) {
        UserResultReport joinLink = new UserResultReport();
        joinLink.setUserCountResultReportModel(report);
        joinLink.setResult(result);
        userResultReportRepo.save(joinLink);
    }
}
