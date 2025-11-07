package ru.viktorgezz.NauJava.domain.report;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.viktorgezz.NauJava.AbstractIntegrationPostgresTest;
import ru.viktorgezz.NauJava.domain.report.dto.ReportUserCountResultsResponse;
import ru.viktorgezz.NauJava.domain.report.service.ReportService;
import ru.viktorgezz.NauJava.domain.result.service.intrf.ResultQueryService;
import ru.viktorgezz.NauJava.domain.user.service.intrf.UserQueryService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@DisplayName("ReportService Error Handling Tests")
class ReportServiceErrorHandlingTest extends AbstractIntegrationPostgresTest {

    @MockitoBean
    private UserQueryService userQueryService;

    @MockitoBean
    private ResultQueryService resultQueryService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserCountResultReportRepo userCountResultReportRepo;

    @AfterEach
    void tearDown() {
        userCountResultReportRepo.deleteAll();
    }

    @Test
    @DisplayName("Ошибка в UserQueryService.computeCountUsers - статус ERROR сохраняется")
    void shouldSetErrorStatusWhenUserQueryServiceThrowsException() {
        // Arrange
        ReportUserCountResultsModel repotCreated = userCountResultReportRepo.save(
                new ReportUserCountResultsModel(StatusReport.CREATED)
        );
        Long idReport = repotCreated.getId();

        when(userQueryService.computeCountUsers())
                .thenThrow(new RuntimeException("Ошибка при подсчете пользователей"));

        when(resultQueryService.findAllWithParticipantUsernameAndTitleTest())
                .thenReturn(List.of());

        // Act
        CompletableFuture<ReportUserCountResultsResponse> resultReportFuture = reportService.generationReport(idReport);

        // Assert - проверяем, что CompletableFuture завершился с ошибкой
        ExecutionException executionException = assertThrows(ExecutionException.class, resultReportFuture::get);
        assertThat(executionException.getCause()).isInstanceOf(RuntimeException.class);
        assertThat(executionException.getCause().getMessage()).isEqualTo("Ошибка при генерации отчета");

        ReportUserCountResultsModel reportAfterError = userCountResultReportRepo.findById(idReport).orElseThrow();
        assertThat(reportAfterError.getStatus()).isEqualTo(StatusReport.ERROR);
    }

    @Test
    @DisplayName("Ошибка в ResultQueryService.findAllWithParticipantUsernameAndTitleTest - статус ERROR сохраняется")
    void shouldSetErrorStatusWhenResultQueryServiceThrowsException() {
        // Arrange
        ReportUserCountResultsModel reportCreated = userCountResultReportRepo.save(
                new ReportUserCountResultsModel(StatusReport.CREATED)
        );
        Long idReport = reportCreated.getId();

        when(userQueryService.computeCountUsers())
                .thenReturn(10L);

        when(resultQueryService.findAllWithParticipantUsernameAndTitleTest())
                .thenThrow(new RuntimeException("Ошибка при получении результатов"));

        // Act
        CompletableFuture<ReportUserCountResultsResponse> resultReportFuture = reportService.generationReport(idReport);

        // Assert - проверяем, что CompletableFuture завершился с ошибкой
        ExecutionException executionException = assertThrows(ExecutionException.class, resultReportFuture::get);
        assertThat(executionException.getCause()).isInstanceOf(RuntimeException.class);
        assertThat(executionException.getCause().getMessage()).isEqualTo("Ошибка при генерации отчета");

        ReportUserCountResultsModel reportAfterError = userCountResultReportRepo.findById(idReport).orElseThrow();
        assertThat(reportAfterError.getStatus()).isEqualTo(StatusReport.ERROR);
    }

    @Test
    @DisplayName("Ошибка в обоих сервисах - статус ERROR сохраняется")
    void shouldSetErrorStatusWhenBothServicesThrowExceptions() {
        // Arrange
        ReportUserCountResultsModel repotCreated = userCountResultReportRepo.save(
                new ReportUserCountResultsModel(StatusReport.CREATED)
        );
        Long idReport = repotCreated.getId();

        when(userQueryService.computeCountUsers())
                .thenThrow(new RuntimeException("Ошибка при подсчете пользователей"));

        when(resultQueryService.findAllWithParticipantUsernameAndTitleTest())
                .thenThrow(new RuntimeException("Ошибка при получении результатов"));

        // Act
        CompletableFuture<ReportUserCountResultsResponse> resultReportFuture = reportService.generationReport(idReport);

        // Assert - проверяем, что CompletableFuture завершился с ошибкой
        assertThrows(ExecutionException.class, () -> {
            try {
                resultReportFuture.get();
            } catch (ExecutionException e) {
                assertThat(e.getCause()).isInstanceOf(RuntimeException.class);
                assertThat(e.getCause().getMessage()).isEqualTo("Ошибка при генерации отчета");
                throw e;
            }
        });

        ReportUserCountResultsModel reportAfterError = userCountResultReportRepo.findById(idReport).orElseThrow();
        assertThat(reportAfterError.getStatus()).isEqualTo(StatusReport.ERROR);
    }

    @Test
    @DisplayName("Проверка сообщения об ошибке в failedFuture")
    void shouldReturnFailedFutureWithErrorMessage() {
        // Arrange
        ReportUserCountResultsModel repotCreated = userCountResultReportRepo.save(
                new ReportUserCountResultsModel(StatusReport.CREATED)
        );
        Long idReport = repotCreated.getId();

        when(userQueryService.computeCountUsers())
                .thenThrow(new RuntimeException("Тестовая ошибка"));

        when(resultQueryService.findAllWithParticipantUsernameAndTitleTest())
                .thenReturn(List.of());

        // Act
        CompletableFuture<ReportUserCountResultsResponse> resultReportFuture = reportService.generationReport(idReport);

        // Assert
        ExecutionException exception = assertThrows(ExecutionException.class, resultReportFuture::get);
        assertThat(exception.getCause()).isInstanceOf(RuntimeException.class);
        assertThat(exception.getCause().getMessage()).isEqualTo("Ошибка при генерации отчета");
    }
}
