package ru.viktorgezz.NauJava.domain.result.service.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.result.Grade;
import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.result.dto.ResultRequestDto;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user_answer.UserAnswer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.viktorgezz.NauJava.util.CreationModel.*;

@DisplayName("ResultScoringCalculator Unit Tests")
class ResultScoringCalculatorTest {

    @Test
    @DisplayName("calculateTotalScoreAndCollectUserAnswers: полный балл и сохранение ответов при верных вариантах SINGLE_CHOICE")
    void calculateTotalScoreAndCollectUserAnswers_ShouldReturnFullScoreAndStoreUserAnswer_WhenSingleChoiceCorrect() {
        User userParticipant = createUserRandom();
        TestModel testModel = createTest("Test", "Description", Status.PUBLIC, userParticipant);
        Question questionSingleChoice = createQuestionSingleChoice("Question?", new BigDecimal("10.00"), testModel);
        questionSingleChoice.setId(1L);

        AnswerOption answerOptionCorrect = createAnswerOption("Correct", true, questionSingleChoice);
        answerOptionCorrect.setId(11L);
        AnswerOption answerOptionWrong = createAnswerOption("Wrong", false, questionSingleChoice);
        answerOptionWrong.setId(12L);

        Map<Long, List<AnswerOption>> idQuestionToAnswerOptions = Map.of(
                questionSingleChoice.getId(),
                List.of(answerOptionCorrect, answerOptionWrong)
        );
        Map<Long, ResultRequestDto.UserAnswerRequestDto> idQuestionToUserAnswer = Map.of(
                questionSingleChoice.getId(),
                new ResultRequestDto.UserAnswerRequestDto("", List.of(answerOptionCorrect.getId()))
        );
        Result resultCompilated = createResultWithTest(userParticipant, testModel, Grade.A, new BigDecimal("0.00"), 60);
        List<UserAnswer> userAnswersProcessedList = new ArrayList<>();

        BigDecimal scoreCalculated = ResultScoringCalculator.calculateTotalScoreAndCollectUserAnswers(
                List.of(questionSingleChoice),
                idQuestionToUserAnswer,
                idQuestionToAnswerOptions,
                resultCompilated,
                userAnswersProcessedList
        );

        assertThat(scoreCalculated).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(userAnswersProcessedList).hasSize(1);
        UserAnswer userAnswerStored = userAnswersProcessedList.getFirst();
        assertThat(userAnswerStored.getCorrect()).isTrue();
        assertThat(userAnswerStored.getAnswerOption().getId()).isEqualTo(answerOptionCorrect.getId());
    }

    @Test
    @DisplayName("calculateTotalScoreAndCollectUserAnswers: нулевой балл при неверном ответе OPEN_TEXT")
    void calculateTotalScoreAndCollectUserAnswers_ShouldReturnZeroScoreAndStoreUserAnswer_WhenOpenTextIncorrect() {
        User userParticipant = createUserRandom();
        TestModel testModel = createTest("Test", "Description", Status.PUBLIC, userParticipant);
        Question questionOpenText = createQuestionOpenText("Text?", new BigDecimal("5.00"), List.of("правильно"), testModel);
        questionOpenText.setId(2L);

        Map<Long, List<AnswerOption>> idQuestionToAnswerOptions = Map.of(
                questionOpenText.getId(),
                List.of()
        );
        Map<Long, ResultRequestDto.UserAnswerRequestDto> idQuestionToUserAnswer = Map.of(
                questionOpenText.getId(),
                new ResultRequestDto.UserAnswerRequestDto("неверно", List.of())
        );
        Result resultCompilated = createResultWithTest(userParticipant, testModel, Grade.B, new BigDecimal("0.00"), 80);
        List<UserAnswer> userAnswersProcessedList = new ArrayList<>();

        BigDecimal scoreCalculated = ResultScoringCalculator.calculateTotalScoreAndCollectUserAnswers(
                List.of(questionOpenText),
                idQuestionToUserAnswer,
                idQuestionToAnswerOptions,
                resultCompilated,
                userAnswersProcessedList
        );

        assertThat(scoreCalculated).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(userAnswersProcessedList).hasSize(1);
        UserAnswer userAnswerStored = userAnswersProcessedList.getFirst();
        assertThat(userAnswerStored.getCorrect()).isFalse();
        assertThat(userAnswerStored.getTextAnswer()).isEqualTo("неверно");
    }

    @Test
    @DisplayName("calculateTotalScoreAndCollectUserAnswers: частичный балл при allowMistakes и одной ошибке MULTIPLE_CHOICE")
    void calculateTotalScoreAndCollectUserAnswers_ShouldReturnPartialScore_WhenAllowMistakesAndOneErrorInMultipleChoice() {
        User userParticipant = createUserRandom();
        TestModel testModel = createTest("Test", "Description", Status.PUBLIC, userParticipant);
        Question questionMultipleChoice = createQuestionMultipleChoiceWithAllowMistakes("Multi?", new BigDecimal("10.00"), testModel);
        questionMultipleChoice.setId(3L);

        AnswerOption answerOptionFirstCorrect = createAnswerOption("First", true, questionMultipleChoice);
        answerOptionFirstCorrect.setId(31L);
        AnswerOption answerOptionSecondCorrect = createAnswerOption("Second", true, questionMultipleChoice);
        answerOptionSecondCorrect.setId(32L);
        AnswerOption answerOptionThirdWrong = createAnswerOption("Third", false, questionMultipleChoice);
        answerOptionThirdWrong.setId(33L);

        Map<Long, List<AnswerOption>> idQuestionToAnswerOptions = Map.of(
                questionMultipleChoice.getId(),
                List.of(answerOptionFirstCorrect, answerOptionSecondCorrect, answerOptionThirdWrong)
        );
        Map<Long, ResultRequestDto.UserAnswerRequestDto> idQuestionToUserAnswer = Map.of(
                questionMultipleChoice.getId(),
                new ResultRequestDto.UserAnswerRequestDto("", List.of(
                        answerOptionFirstCorrect.getId(),
                        answerOptionSecondCorrect.getId(),
                        answerOptionThirdWrong.getId()
                ))
        );
        Result resultCompilated = new Result(150, LocalDateTime.now(), userParticipant, testModel);
        List<UserAnswer> userAnswersProcessedList = new ArrayList<>();

        BigDecimal scoreCalculated = ResultScoringCalculator.calculateTotalScoreAndCollectUserAnswers(
                List.of(questionMultipleChoice),
                idQuestionToUserAnswer,
                idQuestionToAnswerOptions,
                resultCompilated,
                userAnswersProcessedList
        );

        assertThat(scoreCalculated).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(userAnswersProcessedList).hasSize(3);
        long countCorrectAnswers = userAnswersProcessedList.stream()
                .filter(UserAnswer::getCorrect)
                .count();
        assertThat(countCorrectAnswers).isEqualTo(2);
    }
}
