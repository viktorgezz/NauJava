package ru.viktorgezz.NauJava.domain.report.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationControllerTest;
import ru.viktorgezz.NauJava.domain.report.dto.ReportUserCountResultsResponse;
import ru.viktorgezz.NauJava.domain.report.service.intrf.ReportService;
import ru.viktorgezz.NauJava.domain.user.Role;
import ru.viktorgezz.NauJava.exception.BusinessException;
import ru.viktorgezz.NauJava.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Integration Tests for ReportController with Real Auth")
class ReportControllerTest extends AbstractIntegrationControllerTest {

    @MockitoBean
    private ReportService reportService;

    @Override
    protected Role setRole() {
        return Role.MODERATOR;
    }

    @Test
    @DisplayName("POST /reports: успешный запуск генерации отчета (авторизованный запрос)")
    void createTestResult_ShouldReturnId_WhenAuthorized() {
        Long idReportNew = 101L;

        when(reportService.createReport()).thenReturn(idReportNew);

        given()
                .spec(requestSpec)
                .when()
                .post("/reports")
                .then()
                .log().ifError()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo(idReportNew.toString()))
                .header("Content-Type", containsString("application/json"));

        verify(reportService).createReport();
        verify(reportService).generateReportAsync(idReportNew);
    }

    @Test
    @DisplayName("GET /reports/{id}: получение ошибки при отсутствии отчета")
    void getById_ShouldReturnError_WhenReportNotFound() {
        Long idReportMissing = 999L;
        String messageError = "Report not found";

        when(reportService.findById(idReportMissing))
                .thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND, messageError));

        given()
                .spec(requestSpec)
                .pathParam("id_report", idReportMissing)
                .when()
                .get("/reports/{id_report}")
                .then()
                .log().ifValidationFails()
                .statusCode(anyOf(is(HttpStatus.NOT_FOUND.value()), is(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        verify(reportService).findById(idReportMissing);
    }

    @Test
    @DisplayName("GET /reports: получение списка отчетов")
    void getAll_ShouldReturnList_WhenCalled() {
        ReportUserCountResultsResponse reportFirst = new ReportUserCountResultsResponse(
                1L, null, 10L, Collections.emptyList(), 100L, 100L, 200L, LocalDateTime.now()
        );
        ReportUserCountResultsResponse reportSecond = new ReportUserCountResultsResponse(
                2L, null, 5L, Collections.emptyList(), 50L, 50L, 100L, LocalDateTime.now()
        );
        List<ReportUserCountResultsResponse> listExpected = List.of(reportFirst, reportSecond);

        when(reportService.findAll()).thenReturn(listExpected);

        given()
                .spec(requestSpec)
                .when()
                .get("/reports")
                .then()
                .log().ifError()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(2))
                .header("Content-Type", containsString("application/json"));

        verify(reportService).findAll();
    }

    @Test
    @DisplayName("GET /reports: корректная обработка пустого списка")
    void getAll_ShouldReturnEmptyList_WhenNoReports() {
        when(reportService.findAll()).thenReturn(Collections.emptyList());

        given()
                .spec(requestSpec)
                .when()
                .get("/reports")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(0))
                .header("Content-Type", containsString("application/json"));

        verify(reportService).findAll();
    }
}
