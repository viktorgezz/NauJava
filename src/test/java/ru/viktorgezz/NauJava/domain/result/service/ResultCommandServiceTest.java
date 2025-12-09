package ru.viktorgezz.NauJava.domain.result.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.domain.answer_option.repo.AnswerOptionRepo;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.question.repo.QuestionRepo;
import ru.viktorgezz.NauJava.domain.result.Grade;
import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.result.dto.ResultRequestDto;
import ru.viktorgezz.NauJava.domain.result.repo.ResultRepo;
import ru.viktorgezz.NauJava.domain.result.service.intrf.ResultCommandService;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.repo.TestRepo;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;
import ru.viktorgezz.NauJava.domain.user_answer.repo.UserAnswerRepo;
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationPostgresTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.viktorgezz.NauJava.util.CreationModel.*;

@DisplayName("ResultService Integration Tests")
class ResultCommandServiceTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private ResultCommandService resultCommandService;

    @Autowired
    private ResultRepo resultRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TestRepo testRepo;

    @Autowired
    private QuestionRepo questionRepo;

    @Autowired
    private AnswerOptionRepo answerOptionRepo;

    @Autowired
    private UserAnswerRepo userAnswerRepo;

    private User userAuthenticated;
    private TestModel testExisting;

    @BeforeEach
    void setUp() {
        userAuthenticated = userRepo.save(createUserRandom());
        testExisting = testRepo.save(createTest(
                "Test Title",
                "Test Description",
                Status.PUBLIC,
                userAuthenticated
        ));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        userAnswerRepo.deleteAll();
        answerOptionRepo.deleteAll();
        questionRepo.deleteAll();
        resultRepo.deleteAll();
        testRepo.deleteAll();
        userRepo.deleteAll();
    }

    private void setSecurityContext(User userCurrent) {
        UsernamePasswordAuthenticationToken tokenAuthentication = new UsernamePasswordAuthenticationToken(
                userCurrent,
                null,
                userCurrent.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(tokenAuthentication);
    }

    @Test
    @DisplayName("Удаление существующего Result")
    void deleteResult_ShouldDeleteResult_WhenResultExists() {
        setSecurityContext(userAuthenticated);
        Result resultToSave = createResult(userAuthenticated, Grade.B, new BigDecimal("88.0"));
        Result resultSaved = resultRepo.save(resultToSave);
        Long idResultSaved = resultSaved.getId();

        assertThat(resultRepo.existsById(idResultSaved)).isTrue();

        resultCommandService.deleteResult(idResultSaved);

        assertThat(resultRepo.existsById(idResultSaved)).isFalse();
    }

    @Test
    @DisplayName("Выброс EntityNotFoundException при попытке удалить несуществующий Result")
    void deleteResult_ShouldThrowEntityNotFoundException_WhenResultDoesNotExist() {
        Long idResultNonExistent = 999L;
        assertThat(resultRepo.existsById(idResultNonExistent)).isFalse();

        Result resultExisting = resultRepo.save(createResult(userAuthenticated, Grade.C, new BigDecimal("75.0")));
        Long idResultExisting = resultExisting.getId();

        assertThrows(
                EntityNotFoundException.class,
                () -> resultCommandService.deleteResult(idResultNonExistent)
        );
        assertThat(resultRepo.existsById(idResultExisting)).isTrue();
    }

    @Test
    @DisplayName("Создание Result с корректными данными")
    void initiateCompilationResult_ShouldCreateResult_WhenDataIsValid() {
        setSecurityContext(userAuthenticated);
        int timeSpentSecondsExpected = 120;
        ResultRequestDto resultRequestDtoValid = createResultRqDtoWithEmptyAnswers(
                timeSpentSecondsExpected,
                testExisting.getId()
        );
        long countResultInitial = resultRepo.count();

        Long idResultCreated = resultCommandService.initiateCompilationResult(resultRequestDtoValid);

        assertThat(idResultCreated).isNotNull();
        assertThat(resultRepo.count()).isEqualTo(countResultInitial + 1);

        Result resultCreated = resultRepo.findById(idResultCreated).orElseThrow();
        assertThat(resultCreated.getTimeSpentSeconds()).isEqualTo(timeSpentSecondsExpected);
        assertThat(resultCreated.getParticipant().getId()).isEqualTo(userAuthenticated.getId());
        assertThat(resultCreated.getTest().getId()).isEqualTo(testExisting.getId());
        assertThat(resultCreated.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Выброс EntityNotFoundException при попытке создать Result для несуществующего теста")
    void initiateCompilationResult_ShouldThrowEntityNotFoundException_WhenTestDoesNotExist() {
        setSecurityContext(userAuthenticated);
        Long idTestNonExistent = 999999L;
        ResultRequestDto resultRequestDtoInvalid = createResultRqDtoWithEmptyAnswers(120, idTestNonExistent);
        long countResultInitial = resultRepo.count();

        assertThrows(
                EntityNotFoundException.class,
                () -> resultCommandService.initiateCompilationResult(resultRequestDtoInvalid)
        );
        assertThat(resultRepo.count()).isEqualTo(countResultInitial);
    }

    @Test
    @DisplayName("Компиляция Result с правильным ответом на SINGLE_CHOICE вопрос")
    void compilateResultAsync_ShouldCalculateFullScore_WhenAnswerIsCorrect() throws Exception {
        // Arrange
        setSecurityContext(userAuthenticated);
        BigDecimal pointQuestion = new BigDecimal("10.00");
        testExisting.setScoreMax(pointQuestion);
        testRepo.save(testExisting);

        Question question = createQuestionSingleChoice("Test question?", pointQuestion, testExisting);
        question = questionRepo.save(question);

        AnswerOption correctOption = createAnswerOption("Correct", true, question);
        AnswerOption wrongOption = createAnswerOption("Wrong", false, question);
        correctOption = answerOptionRepo.save(correctOption);
        answerOptionRepo.save(wrongOption);

        Result result = createResultWithoutGrade(60, userAuthenticated, testExisting);
        result = resultRepo.save(result);

        Map<Long, ResultRequestDto.UserAnswerRequestDto> userAnswers = Map.of(
                question.getId(),
                createUserAnswerRequestDtoWithSelectedOptions(List.of(correctOption.getId()))
        );

        CompletableFuture<Result> futureResult = resultCommandService.compilateResultAsync(userAnswers, result.getId());
        Result compiledResult = futureResult.get();

        assertThat(compiledResult.getScore()).isEqualByComparingTo(pointQuestion);
        assertThat(compiledResult.getGrade()).isEqualTo(Grade.A);
    }

    @Test
    @DisplayName("Компиляция Result с неправильным ответом на SINGLE_CHOICE вопрос")
    void compilateResultAsync_ShouldCalculateZeroScore_WhenAnswerIsWrong() throws Exception {
        setSecurityContext(userAuthenticated);
        BigDecimal pointQuestion = new BigDecimal("10.00");
        testExisting.setScoreMax(pointQuestion);
        testRepo.save(testExisting);

        Question question = createQuestionSingleChoice("Test question?", pointQuestion, testExisting);
        question = questionRepo.save(question);

        AnswerOption correctOption = createAnswerOption("Correct", true, question);
        AnswerOption wrongOption = createAnswerOption("Wrong", false, question);
        answerOptionRepo.save(correctOption);
        wrongOption = answerOptionRepo.save(wrongOption);

        Result result = createResultWithoutGrade(60, userAuthenticated, testExisting);
        result = resultRepo.save(result);

        Map<Long, ResultRequestDto.UserAnswerRequestDto> userAnswers = Map.of(
                question.getId(),
                createUserAnswerRequestDtoWithSelectedOptions(List.of(wrongOption.getId()))
        );

        CompletableFuture<Result> futureResult = resultCommandService.compilateResultAsync(userAnswers, result.getId());
        Result compiledResult = futureResult.get();

        assertThat(compiledResult.getScore()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(compiledResult.getGrade()).isEqualTo(Grade.F);
    }

    @Test
    @DisplayName("Компиляция Result с правильным текстовым ответом на OPEN_TEXT вопрос")
    void compilateResultAsync_ShouldCalculateFullScore_WhenOpenTextAnswerIsCorrect() throws Exception {
        // Arrange
        setSecurityContext(userAuthenticated);
        BigDecimal pointQuestion = new BigDecimal("10.00");
        String correctAnswer = "правильный ответ";
        testExisting.setScoreMax(pointQuestion);
        testRepo.save(testExisting);

        Question question = createQuestionOpenText(
                "Введите ответ:",
                pointQuestion,
                List.of(correctAnswer),
                testExisting
        );
        question = questionRepo.save(question);

        Result result = createResultWithoutGrade(60, userAuthenticated, testExisting);
        result = resultRepo.save(result);

        Map<Long, ResultRequestDto.UserAnswerRequestDto> userAnswers = Map.of(
                question.getId(),
                createUserAnswerRequestDtoWithTextAnswer(correctAnswer)
        );

        // Act
        CompletableFuture<Result> futureResult = resultCommandService.compilateResultAsync(userAnswers, result.getId());
        Result compiledResult = futureResult.get();

        // Assert
        assertThat(compiledResult.getScore()).isEqualByComparingTo(pointQuestion);
        assertThat(compiledResult.getGrade()).isEqualTo(Grade.A);
    }

    @Test
    @DisplayName("Компиляция Result с неправильным текстовым ответом на OPEN_TEXT вопрос")
    void compilateResultAsync_ShouldCalculateZeroScore_WhenOpenTextAnswerIsWrong() throws Exception {
        // Arrange
        setSecurityContext(userAuthenticated);
        BigDecimal pointQuestion = new BigDecimal("10.00");
        testExisting.setScoreMax(pointQuestion);
        testRepo.save(testExisting);

        Question question = createQuestionOpenText(
                "Введите ответ:",
                pointQuestion,
                List.of("правильный ответ"),
                testExisting
        );
        question = questionRepo.save(question);

        Result result = createResultWithoutGrade(60, userAuthenticated, testExisting);
        result = resultRepo.save(result);

        Map<Long, ResultRequestDto.UserAnswerRequestDto> userAnswers = Map.of(
                question.getId(),
                createUserAnswerRequestDtoWithTextAnswer("неправильный ответ")
        );

        // Act
        CompletableFuture<Result> futureResult = resultCommandService.compilateResultAsync(userAnswers, result.getId());
        Result compiledResult = futureResult.get();

        // Assert
        assertThat(compiledResult.getScore()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(compiledResult.getGrade()).isEqualTo(Grade.F);
    }

    @Test
    @DisplayName("Компиляция Result сохраняет UserAnswers в базу данных")
    void compilateResultAsync_ShouldSaveUserAnswers_WhenCalled() throws Exception {
        // Arrange
        setSecurityContext(userAuthenticated);
        BigDecimal pointQuestion = new BigDecimal("10.00");
        testExisting.setScoreMax(pointQuestion);
        testRepo.save(testExisting);

        Question question = createQuestionSingleChoice("Test question?", pointQuestion, testExisting);
        question = questionRepo.save(question);

        AnswerOption correctOption = createAnswerOption("Correct", true, question);
        correctOption = answerOptionRepo.save(correctOption);

        Result result = createResultWithoutGrade(60, userAuthenticated, testExisting);
        result = resultRepo.save(result);

        long initialUserAnswerCount = userAnswerRepo.count();

        Map<Long, ResultRequestDto.UserAnswerRequestDto> userAnswers = Map.of(
                question.getId(),
                createUserAnswerRequestDtoWithSelectedOptions(List.of(correctOption.getId()))
        );

        // Act
        resultCommandService.compilateResultAsync(userAnswers, result.getId()).get();

        // Assert
        assertThat(userAnswerRepo.count()).isEqualTo(initialUserAnswerCount + 1);
    }

    @Test
    @DisplayName("Выброс EntityNotFoundException при компиляции несуществующего Result")
    void compilateResultAsync_ShouldThrowEntityNotFoundException_WhenResultDoesNotExist() {
        // Arrange
        Long idResultNonExistent = 999999L;
        Map<Long, ResultRequestDto.UserAnswerRequestDto> userAnswers = Map.of();

        // Act & Assert
        assertThrows(
                EntityNotFoundException.class,
                () -> resultCommandService.compilateResultAsync(userAnswers, idResultNonExistent)
        );
    }

    @Test
    @DisplayName("Частичное начисление баллов при allowMistakes=true и одной ошибке")
    void compilateResultAsync_ShouldCalculatePartialScore_WhenAllowMistakesAndOneError() throws Exception {
        setSecurityContext(userAuthenticated);
        BigDecimal pointQuestion = new BigDecimal("10.00");
        testExisting.setScoreMax(pointQuestion);
        testRepo.save(testExisting);

        Question question = createQuestionMultipleChoiceWithAllowMistakes(
                "Выберите правильные ответы:",
                pointQuestion,
                testExisting
        );
        question = questionRepo.save(question);

        // 3 варианта ответа: 2 правильных, 1 неправильный
        AnswerOption correctOption1 = createAnswerOption("Correct 1", true, question);
        AnswerOption correctOption2 = createAnswerOption("Correct 2", true, question);
        AnswerOption wrongOption = createAnswerOption("Wrong", false, question);
        correctOption1 = answerOptionRepo.save(correctOption1);
        correctOption2 = answerOptionRepo.save(correctOption2);
        wrongOption = answerOptionRepo.save(wrongOption);

        Result result = createResultWithoutGrade(60, userAuthenticated, testExisting);
        result = resultRepo.save(result);

        // Пользователь выбирает все 3 варианта (2 правильных + 1 неправильный)
        Map<Long, ResultRequestDto.UserAnswerRequestDto> userAnswers = Map.of(
                question.getId(),
                createUserAnswerRequestDtoWithSelectedOptions(
                        List.of(correctOption1.getId(), correctOption2.getId(), wrongOption.getId())
                )
        );

        CompletableFuture<Result> futureResult = resultCommandService.compilateResultAsync(userAnswers, result.getId());
        Result compiledResult = futureResult.get();

        // Формула: point * (1 - errors/correctOptionsCount) = 10 * (1 - 1/2) = 5.00
        BigDecimal expectedScore = new BigDecimal("5.00");
        assertThat(compiledResult.getScore()).isEqualByComparingTo(expectedScore);
    }
}
