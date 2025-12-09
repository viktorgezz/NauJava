package ru.viktorgezz.NauJava.domain.test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.viktorgezz.NauJava.domain.question.Type;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.dto.TestMetadataRequestDto;
import ru.viktorgezz.NauJava.domain.test.dto.TestToPassDto;
import ru.viktorgezz.NauJava.domain.test.dto.TestUpdateTestContentDto;
import ru.viktorgezz.NauJava.domain.test.service.intrf.TestCommandService;
import ru.viktorgezz.NauJava.domain.test.service.intrf.TestQueryService;
import ru.viktorgezz.NauJava.domain.user.Role;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationControllerTest;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.viktorgezz.NauJava.util.CreationModel.*;

@DisplayName("TestController Integration Tests")
class TestControllerTest extends AbstractIntegrationControllerTest {

    @MockitoBean
    private TestQueryService testQueryService;

    @MockitoBean
    private TestCommandService testCommandService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected Role setRole() {
        return Role.USER;
    }

    @Test
    @DisplayName("PUT /tests/content: успешное обновление контента теста")
    void updateTestContent_ShouldReturnOk_WhenRequestIsValid() throws Exception {
        List<TestUpdateTestContentDto.AnswerOptionDto> answerOptionsDto = List.of(createAnswerOptionDtoNew());
        TestUpdateTestContentDto.QuestionDto questionDto = createQuestionDtoSingleChoice(answerOptionsDto);
        TestUpdateTestContentDto testUpdateDto = new TestUpdateTestContentDto(10L, List.of(questionDto));

        given()
                .spec(requestSpec)
                .body(objectMapper.writeValueAsString(testUpdateDto))
                .when()
                .put("/tests/content")
                .then()
                .statusCode(HttpStatus.OK.value());

        verify(testCommandService).updateTestContent(any(TestUpdateTestContentDto.class));
    }

    @Test
    @DisplayName("GET /tests/content: возврат контента теста по id")
    void getTestContent_ShouldReturnDto_WhenTestExists() {
        Long idTest = 20L;
        List<TestUpdateTestContentDto.AnswerOptionDto> answerOptionsDto = List.of(createAnswerOptionDtoNew());
        TestUpdateTestContentDto.QuestionDto questionDto = createQuestionDtoSingleChoice(answerOptionsDto);
        TestUpdateTestContentDto testUpdateDtoExpected = new TestUpdateTestContentDto(idTest, List.of(questionDto));

        when(testQueryService.findByIdWithContent(idTest)).thenReturn(testUpdateDtoExpected);

        given()
                .spec(requestSpec)
                .queryParam("id", idTest)
                .when()
                .get("/tests/content")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("idTest", equalTo(idTest.intValue()))
                .body("questions", hasSize(1))
                .body("questions[0].text", equalTo(questionDto.text()))
                .body("questions[0].type", equalTo(questionDto.type().name()));

        verify(testQueryService).findByIdWithContent(idTest);
    }

    @Test
    @DisplayName("POST /tests/metadata: успешное создание или обновление метаданных теста")
    void createTest_ShouldReturnId_WhenRequestIsValid() throws Exception {
        Long idTestExpected = 30L;
        TestMetadataRequestDto requestDto = new TestMetadataRequestDto(
                null,
                "Title",
                "Description",
                Status.PUBLIC,
                List.of("Topic1", "Topic2")
        );

        when(testCommandService.updateTestMetadata(any(TestMetadataRequestDto.class))).thenReturn(idTestExpected);

        given()
                .spec(requestSpec)
                .body(objectMapper.writeValueAsString(requestDto))
                .when()
                .post("/tests/metadata")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo(idTestExpected.toString()));

        verify(testCommandService).updateTestMetadata(any(TestMetadataRequestDto.class));
    }

    @Test
    @DisplayName("GET /tests/{id}: возврат теста для прохождения")
    void findTestToPassById_ShouldReturnDto_WhenTestExists() {
        Long idTest = 40L;
        TestToPassDto.AnswerOptionDto answerOptionDto = new TestToPassDto.AnswerOptionDto(1L, "Answer");
        TestToPassDto.QuestionDto questionDto = new TestToPassDto.QuestionDto(
                2L,
                "Question text",
                Type.SINGLE_CHOICE,
                List.of(answerOptionDto)
        );
        TestToPassDto testToPassDtoExpected = new TestToPassDto(idTest, List.of(questionDto));

        when(testQueryService.findTestToPassById(idTest)).thenReturn(testToPassDtoExpected);

        given()
                .spec(requestSpec)
                .pathParam("id", idTest)
                .when()
                .get("/tests/{id}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("idTest", equalTo(idTest.intValue()))
                .body("questionsDto", hasSize(1))
                .body("questionsDto[0].idQuestion", equalTo(questionDto.idQuestion().intValue()))
                .body("questionsDto[0].type", equalTo(questionDto.type().name()));

        verify(testQueryService).findTestToPassById(idTest);
    }

    @Test
    @DisplayName("GET /tests/title: возврат пагинированного списка по заголовку")
    void getTestsByTitle_ShouldReturnPagedMetadata_WhenTitleExists() {
        String titleQuery = "Java";
        User authorTest = createUserRandom();
        authorTest.setId(1L);
        TestModel testModel = createTest("Java Basics", "Desc", Status.PUBLIC, authorTest);
        testModel.setId(50L);

        when(testQueryService.findByTitle(any(), any()))
                .thenReturn(new PageImpl<>(List.of(testModel)));

        given()
                .spec(requestSpec)
                .queryParam("title", titleQuery)
                .when()
                .get("/tests/title")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("_embedded.testMetadataResponseDtoes", hasSize(1))
                .body("_embedded.testMetadataResponseDtoes[0].title", equalTo("Java Basics"));

        verify(testQueryService).findByTitle(any(), any());
    }
}
