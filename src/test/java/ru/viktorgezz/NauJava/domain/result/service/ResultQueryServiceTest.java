package ru.viktorgezz.NauJava.domain.result.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.domain.answer_option.repo.AnswerOptionRepo;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.question.repo.QuestionRepo;
import ru.viktorgezz.NauJava.domain.result.Grade;
import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.result.dto.ResultResponseDto;
import ru.viktorgezz.NauJava.domain.result.repo.ResultRepo;
import ru.viktorgezz.NauJava.domain.result.service.intrf.ResultQueryService;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.repo.TestRepo;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;
import ru.viktorgezz.NauJava.domain.user_answer.UserAnswer;
import ru.viktorgezz.NauJava.domain.user_answer.repo.UserAnswerRepo;
import ru.viktorgezz.NauJava.exception.BusinessException;
import ru.viktorgezz.NauJava.exception.ErrorCode;
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationPostgresTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.viktorgezz.NauJava.util.CreationModel.*;

@DisplayName("ResultQueryService Integration Tests")
class ResultQueryServiceTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private ResultQueryService resultQueryService;

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

    private User user1;

    @BeforeEach
    void setUp() {
        user1 = userRepo.save(createUserRandom());
        User user2 = userRepo.save(createUserRandom());

        resultRepo.save(createResult(user2, Grade.A, new BigDecimal("92.0")));
    }

    @AfterEach
    void tearDown() {
        userAnswerRepo.deleteAll();
        answerOptionRepo.deleteAll();
        questionRepo.deleteAll();
        resultRepo.deleteAll();
        testRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("Получение ResultRsDto для результата с вопросом SINGLE_CHOICE")
    void getTestResultDto_ShouldReturnDto_WhenResultWithSingleChoiceQuestionExists() {
        TestModel test = testRepo.save(createTest("Test", "Description", Status.PUBLIC, user1));
        test.setScoreMax(new BigDecimal("10.00"));
        testRepo.save(test);

        Question question = createQuestionSingleChoice("Question?", new BigDecimal("10.00"), test);
        question = questionRepo.save(question);

        AnswerOption correctOption = answerOptionRepo.save(createAnswerOption("Correct", true, question));
        answerOptionRepo.save(createAnswerOption("Wrong", false, question));

        Result result = createResultWithTest(user1, test, Grade.A, new BigDecimal("10.00"), 60);
        result = resultRepo.save(result);

        UserAnswer userAnswer = createUserAnswerForChoiceQuestion(result, question, correctOption, true);
        userAnswerRepo.save(userAnswer);

        ResultResponseDto dto = resultQueryService.getTestResultDto(result.getId());

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(result.getId());
        assertThat(dto.score()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(dto.grade()).isEqualTo(Grade.A);
        assertThat(dto.questionsDto()).hasSize(1);

        ResultResponseDto.QuestionDto questionDto = dto.questionsDto().getFirst();
        assertThat(questionDto.idQuestion()).isEqualTo(question.getId());
        assertThat(questionDto.userAnswersDto()).isNotEmpty();
        assertThat(questionDto.userAnswersDto().stream().anyMatch(a -> a.isSelected() && a.idAnswerOption().equals(correctOption.getId()))).isTrue();
    }

    @Test
    @DisplayName("Получение ResultRsDto для результата с вопросом OPEN_TEXT")
    void getTestResultDto_ShouldReturnDto_WhenResultWithOpenTextQuestionExists() {
        TestModel test = testRepo.save(createTest("Test", "Description", Status.PUBLIC, user1));
        test.setScoreMax(new BigDecimal("10.00"));
        testRepo.save(test);

        String correctAnswer = "правильный ответ";
        Question question = createQuestionOpenText("Question?", new BigDecimal("10.00"), List.of(correctAnswer), test);
        question = questionRepo.save(question);

        Result result = createResultWithTest(user1, test, Grade.A, new BigDecimal("10.00"), 60);
        result = resultRepo.save(result);

        UserAnswer userAnswer = createUserAnswerForOpenTextQuestion(result, question, correctAnswer, true);
        userAnswerRepo.save(userAnswer);

        ResultResponseDto dto = resultQueryService.getTestResultDto(result.getId());

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(result.getId());
        assertThat(dto.questionsDto()).hasSize(1);

        ResultResponseDto.QuestionDto questionDto = dto.questionsDto().getFirst();
        assertThat(questionDto.idQuestion()).isEqualTo(question.getId());
        assertThat(questionDto.userAnswersDto()).isNotEmpty();

        ResultResponseDto.UserAnswerResponseDto answerDto = questionDto.userAnswersDto().getFirst();
        assertThat(answerDto.userTextAnswer()).isEqualTo(correctAnswer);
        assertThat(answerDto.textAnswersTrue()).contains(correctAnswer);
    }

    @Test
    @DisplayName("Получение ResultRsDto с несколькими вопросами разных типов")
    void getTestResultDto_ShouldReturnDto_WhenResultWithMultipleQuestionsExists() {
        TestModel test = testRepo.save(createTest("Test", "Description", Status.PUBLIC, user1));
        test.setScoreMax(new BigDecimal("20.00"));
        testRepo.save(test);

        Question choiceQuestion = createQuestionSingleChoice("Choice?", new BigDecimal("10.00"), test);
        choiceQuestion = questionRepo.save(choiceQuestion);

        Question textQuestion = createQuestionOpenText("Text?", new BigDecimal("10.00"), List.of("answer"), test);
        textQuestion = questionRepo.save(textQuestion);

        AnswerOption option = answerOptionRepo.save(createAnswerOption("Option", true, choiceQuestion));

        Result result = createResultWithTest(user1, test, Grade.A, new BigDecimal("20.00"), 120);
        result = resultRepo.save(result);

        userAnswerRepo.save(createUserAnswerForChoiceQuestion(result, choiceQuestion, option, true));
        userAnswerRepo.save(createUserAnswerForOpenTextQuestion(result, textQuestion, "answer", true));

        ResultResponseDto dto = resultQueryService.getTestResultDto(result.getId());

        assertThat(dto).isNotNull();
        assertThat(dto.questionsDto()).hasSize(2);

        List<Long> questionIds = dto.questionsDto().stream()
                .map(ResultResponseDto.QuestionDto::idQuestion)
                .toList();
        assertThat(questionIds).contains(choiceQuestion.getId(), textQuestion.getId());
    }

    @Test
    @DisplayName("Выброс BusinessException при запросе несуществующего Result")
    void getTestResultDto_ShouldThrowBusinessException_WhenResultDoesNotExist() {
        Long idResultNonExistent = 999999L;

        assertThatThrownBy(() -> resultQueryService.getTestResultDto(idResultNonExistent))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException businessEx = (BusinessException) ex;
                    assertThat(businessEx.getErrorCode()).isEqualTo(ErrorCode.RESULT_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("Получение ResultRsDto содержит правильные метаданные результата")
    void getTestResultDto_ShouldReturnCorrectMetadata() {
        TestModel test = testRepo.save(createTest("Test", "Description", Status.PUBLIC, user1));
        BigDecimal scoreMax = new BigDecimal("100.00");
        test.setScoreMax(scoreMax);
        testRepo.save(test);

        BigDecimal score = new BigDecimal("85.00");
        int timeSpentSeconds = 300;
        Result result = createResultWithTest(user1, test, Grade.B, score, timeSpentSeconds);
        result = resultRepo.save(result);

        ResultResponseDto dto = resultQueryService.getTestResultDto(result.getId());

        assertThat(dto.id()).isEqualTo(result.getId());
        assertThat(dto.score()).isEqualByComparingTo(score);
        assertThat(dto.scoreMax()).isEqualByComparingTo(scoreMax);
        assertThat(dto.grade()).isEqualTo(Grade.B);
        assertThat(dto.timeSpentSeconds()).isEqualTo(timeSpentSeconds);
        assertThat(dto.completedAt()).isNotNull();
    }

    @Test
    @DisplayName("findAllWithParticipantUsernameAndTitleTest: возврат результатов с загруженными participant и test")
    void findAllWithParticipantUsernameAndTitleTest_ShouldReturnResultsWithLoadedParticipantAndTest_WhenResultsExist() {
        resultRepo.deleteAll();
        User userParticipantFirst = userRepo.save(createUserRandom());
        User userParticipantSecond = userRepo.save(createUserRandom());
        TestModel testFirst = testRepo.save(createTest("Test First", "Description First", Status.PUBLIC, user1));
        TestModel testSecond = testRepo.save(createTest("Test Second", "Description Second", Status.PUBLIC, user1));

        Result resultFirstSaved = resultRepo.save(createResultWithTest(userParticipantFirst, testFirst, Grade.A, new BigDecimal("90.00"), 120));
        Result resultSecondSaved = resultRepo.save(createResultWithTest(userParticipantSecond, testSecond, Grade.B, new BigDecimal("80.00"), 150));

        List<Result> resultsFound = resultQueryService.findAllWithParticipantUsernameAndTitleTest();

        assertThat(resultsFound).hasSize(2);
        resultsFound.forEach(resultFound -> {
            assertThat(resultFound.getParticipant()).isNotNull();
            assertThat(resultFound.getParticipant().getUsername()).isNotNull();
            assertThat(resultFound.getTest()).isNotNull();
            assertThat(resultFound.getTest().getTitle()).isNotNull();
        });
        List<Long> idsResultFound = resultsFound.stream()
                .map(Result::getId)
                .toList();
        assertThat(idsResultFound).containsExactlyInAnyOrder(resultFirstSaved.getId(), resultSecondSaved.getId());
    }

    @Test
    @DisplayName("findAllWithParticipantUsernameAndTitleTest: возврат пустого списка когда результатов нет")
    void findAllWithParticipantUsernameAndTitleTest_ShouldReturnEmptyList_WhenNoResultsExist() {
        resultRepo.deleteAll();
        List<Result> resultsFound = resultQueryService.findAllWithParticipantUsernameAndTitleTest();
        assertThat(resultsFound).isEmpty();
    }
}
