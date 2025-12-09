package ru.viktorgezz.NauJava.domain.result.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.viktorgezz.NauJava.domain.question.Type;
import ru.viktorgezz.NauJava.domain.result.Grade;
import ru.viktorgezz.NauJava.domain.result.dto.ResultRequestDto;
import ru.viktorgezz.NauJava.domain.result.dto.ResultResponseDto;
import ru.viktorgezz.NauJava.domain.result.service.intrf.ResultCommandService;
import ru.viktorgezz.NauJava.domain.result.service.intrf.ResultQueryService;
import ru.viktorgezz.NauJava.domain.user.Role;
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationControllerTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.viktorgezz.NauJava.util.CreationModel.createUserAnswerRequestDtoWithSelectedOptions;

/**
 * Тесты веб-слоя для {@link ResultController}.
 */
@DisplayName("ResultController Integration Tests")
class ResultControllerTest extends AbstractIntegrationControllerTest {

    @MockitoBean
    private ResultQueryService resultQueryService;

    @MockitoBean
    private ResultCommandService resultCommandService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void tearDown() {
        RestAssured.reset();
    }

    @Override
    protected Role setRole() {
        return Role.USER;
    }

    @Test
    @DisplayName("POST /results: успешное создание результата теста")
    void createTestResult_ShouldReturnIdResult_WhenRequestIsValid() throws Exception {
        Long idResultExpected = 100L;
        Long idTest = 1L;
        int timeSpentSeconds = 120;
        Map<Long, ResultRequestDto.UserAnswerRequestDto> userAnswers = Map.of(
                1L,
                createUserAnswerRequestDtoWithSelectedOptions(List.of(1L, 2L))
        );
        ResultRequestDto resultRequestDto = new ResultRequestDto(timeSpentSeconds, idTest, userAnswers);

        when(resultCommandService.initiateCompilationResult(any(ResultRequestDto.class)))
                .thenReturn(idResultExpected);
        when(resultCommandService.compilateResultAsync(any(), any(Long.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        given()
                .spec(requestSpec)
                .body(objectMapper.writeValueAsString(resultRequestDto))
                .when()
                .post("/results")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo(idResultExpected.toString()));

        verify(resultCommandService).initiateCompilationResult(any(ResultRequestDto.class));
        verify(resultCommandService).compilateResultAsync(any(), any(Long.class));
    }

    @Test
    @DisplayName("POST /results: возврат ошибки при невалидном запросе")
    void createTestResult_ShouldReturnError_WhenRequestIsInvalid() throws Exception {
        int timeSpentSeconds = 120;
        Map<Long, ResultRequestDto.UserAnswerRequestDto> userAnswers = Map.of();
        ResultRequestDto resultRequestDtoInvalid = new ResultRequestDto(timeSpentSeconds, null, userAnswers);

        given()
                .spec(requestSpec)
                .body(objectMapper.writeValueAsString(resultRequestDtoInvalid))
                .when()
                .post("/results")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("GET /results/{id}: успешное получение результата теста")
    void findTestResult_ShouldReturnResultResponseDto_WhenResultExists() {
        Long idResult = 100L;
        Long idTest = 1L;
        BigDecimal score = new BigDecimal("95.00");
        BigDecimal scoreMax = new BigDecimal("100.00");
        Grade grade = Grade.A;
        int timeSpentSeconds = 120;
        LocalDateTime completedAt = LocalDateTime.now();

        ResultResponseDto.QuestionDto questionDto = new ResultResponseDto.QuestionDto(
                1L,
                "Question text?",
                Type.SINGLE_CHOICE,
                List.of(new ResultResponseDto.UserAnswerResponseDto(
                        1L,
                        "Answer text",
                        true,
                        true,
                        null,
                        null
                ))
        );

        ResultResponseDto resultResponseDtoExpected = new ResultResponseDto(
                idResult,
                idTest,
                score,
                scoreMax,
                grade,
                timeSpentSeconds,
                completedAt,
                List.of(questionDto)
        );

        when(resultQueryService.getTestResultDto(idResult))
                .thenReturn(resultResponseDtoExpected);

        given()
                .spec(requestSpec)
                .pathParam("id", idResult)
                .when()
                .get("/results/{id}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(idResult.intValue()))
                .body("idTest", equalTo(idTest.intValue()))
                .body("score", equalTo(95.00f))
                .body("grade", equalTo("A"));

        verify(resultQueryService).getTestResultDto(idResult);
    }

    @Test
    @DisplayName("GET /results/{id}: возврат ошибки при отсутствии результата")
    void findTestResult_ShouldReturnError_WhenResultDoesNotExist() {
        Long idResultNonExistent = 999L;

        when(resultQueryService.getTestResultDto(idResultNonExistent))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Result not found"));

        given()
                .spec(requestSpec)
                .pathParam("id", idResultNonExistent)
                .when()
                .get("/results/{id}")
                .then()
                .statusCode(anyOf(is(HttpStatus.NOT_FOUND.value()), is(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        verify(resultQueryService).getTestResultDto(idResultNonExistent);
    }
}
