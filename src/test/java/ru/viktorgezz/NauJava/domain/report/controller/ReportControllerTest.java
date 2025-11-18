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

    private static final String CONTENT_TYPE_KEY = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "application/json";

    @MockitoBean
    private ReportService reportService;

    @Override
    protected Role setRole() {
        return Role.MODERATOR;
    }

    /**
     * <p>Тестирование HTTP POST запроса на {@code /reports} для запуска генерации нового отчета.
     * Проверяется, что запрос, выполненный авторизованным пользователем,
     * возвращает ID нового отчета и вызывает соответствующие методы сервиса (создание и асинхронную генерацию).</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Определяется ожидаемый ID нового отчета ({@code 101L}).</li>
     * <li>Настраивается Мок-объект {@code reportService}: при вызове {@code createReport()} возвращается этот ID.</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Отправляется POST запрос на {@code /reports}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что HTTP статус ответа {@code 200 OK}.</li>
     * <li>Проверить, что тело ответа содержит ожидаемый ID в виде строки.</li>
     * <li>Проверить, что метод {@code reportService.createReport()} был вызван.</li>
     * <li>Проверить, что метод {@code reportService.generateReportAsync(idReportNew)} был вызван.</li>
     * </ul>
     * </ol></b>
     */
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
                .header(CONTENT_TYPE_KEY, containsString(CONTENT_TYPE_VALUE));

        verify(reportService).createReport();
        verify(reportService).generateReportAsync(idReportNew);
    }

    /**
     * <p>Тестирование HTTP GET запроса на {@code /reports/{id}} при попытке получить
     * отчет, который не существует в системе. Ожидается, что будет возвращена ошибка (например, {@code 404 NOT FOUND}
     * или {@code 500 INTERNAL SERVER ERROR}, обрабатывающий {@code BusinessException}).</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Определяется ID отсутствующего отчета ({@code 999L}).</li>
     * <li>Настраивается Мок-объект {@code reportService}: при вызове {@code findById(idReportMissing)} выбрасывается {@code BusinessException} (имитация "Отчет не найден").</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Отправляется GET запрос на {@code /reports/999}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что HTTP статус ответа соответствует ошибке (404 или 500).</li>
     * <li>Проверить, что метод {@code reportService.findById(idReportMissing)} был вызван.</li>
     * </ul>
     * </ol></b>
     */
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

    /**
     * <p>Тестирование HTTP GET запроса на {@code /reports} для получения списка всех отчетов.
     * Проверяется, что метод возвращает корректный список объектов DTO, полученных от сервиса.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Создается список из двух ожидаемых объектов DTO ({@code ReportUserCountResultsResponse}).</li>
     * <li>Настраивается Мок-объект {@code reportService}: при вызове {@code findAll()} возвращается ожидаемый список.</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Отправляется GET запрос на {@code /reports}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что HTTP статус ответа {@code 200 OK}.</li>
     * <li>Проверить, что возвращенный JSON-массив содержит ровно 2 элемента.</li>
     * <li>Проверить, что метод {@code reportService.findAll()} был вызван.</li>
     * </ul>
     * </ol></b>
     */
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
                .header(CONTENT_TYPE_KEY, containsString(CONTENT_TYPE_VALUE));

        verify(reportService).findAll();
    }

    /**
     * <p>Тестирование HTTP GET запроса на {@code /reports} при отсутствии отчетов в системе.
     * Проверяется, что контроллер корректно возвращает пустой JSON-массив с HTTP статусом {@code 200 OK}.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Настраивается Мок-объект {@code reportService}: при вызове {@code findAll()} возвращается пустой список ({@code Collections.emptyList()}).</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Отправляется GET запрос на {@code /reports}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что HTTP статус ответа {@code 200 OK}.</li>
     * <li>Проверить, что возвращенный JSON-массив содержит 0 элементов (пустой).</li>
     * <li>Проверить, что метод {@code reportService.findAll()} был вызван.</li>
     * </ul>
     * </ol></b>
     */
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
                .header(CONTENT_TYPE_KEY, containsString(CONTENT_TYPE_VALUE));

        verify(reportService).findAll();
    }
}
