package ru.viktorgezz.NauJava.util;

import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.question.Type;
import ru.viktorgezz.NauJava.domain.report.StatusReport;
import ru.viktorgezz.NauJava.domain.result.dto.ResultRequestDto;
import ru.viktorgezz.NauJava.domain.report.dto.ResultResponse;
import ru.viktorgezz.NauJava.domain.report.model.ReportResultData;
import ru.viktorgezz.NauJava.domain.report.model.ReportUserCountResultsModel;
import ru.viktorgezz.NauJava.domain.result.Grade;
import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.dto.TestUpdateTestContentDto;
import ru.viktorgezz.NauJava.domain.topic.Topic;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user_answer.UserAnswer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static ru.viktorgezz.NauJava.util.GeneratorRandom.*;

/**
 * Утилиты для создания тестовых моделей.
 */
public class CreationModel {

    public static Result createResult(User user, Grade grade, BigDecimal score) {
        Result result = new Result();
        result.setParticipant(user);
        result.setGrade(grade);
        result.setScore(score);
        result.setCompletedAt(LocalDateTime.now());
        return result;
    }

    public static ResultRequestDto createResultRqDtoWithEmptyAnswers(int timeSpentSeconds, Long idTest) {
        return new ResultRequestDto(timeSpentSeconds, idTest, Collections.emptyMap());
    }

    public static TestModel createTest(
            String title,
            String description,
            Status status,
            User author
    ) {
        TestModel testModel = new TestModel();
        testModel.setTitle(title);
        testModel.setDescription(description);
        testModel.setStatus(status);
        testModel.setAuthor(author);
        return testModel;
    }

    public static TestModel createTestRadom(User author) {
        return createTest(
                GeneratorRandom.getRandomTestTitleRandom(),
                GeneratorRandom.getRandomString(10),
                Status.PUBLIC,
                author
        );
    }

    public static ReportUserCountResultsModel createUserCountResultReportModel(
            StatusReport status,
            Long countUsers,
            ReportResultData reportResultData,
            LocalDateTime completedAt,
            Long timeSpentSearchingForUsersMillis,
            Long timeSpentSearchingForResultsMillis,
            Long timeSpentCommonMillis
    ) {
        ReportUserCountResultsModel reportUserCountResultsModel = new ReportUserCountResultsModel(
                status,
                countUsers,
                timeSpentSearchingForUsersMillis,
                timeSpentSearchingForResultsMillis,
                timeSpentCommonMillis,
                completedAt
        );
        reportUserCountResultsModel.setReportResultData(reportResultData);
        return reportUserCountResultsModel;
    }

    public static ResultResponse createResultResponse(
            BigDecimal score,
            Grade grade,
            String usernameParticipant,
            String titleTest
    ) {
        return new ResultResponse(
                score,
                grade,
                new Random().nextInt() * 15,
                LocalDateTime.now(),
                usernameParticipant,
                titleTest
        );
    }

    public static User createUserRandom() {
        return GeneratorRandom.getRandomUser();
    }

    public static Topic createTopic(String topicTitle) {
        return new Topic(topicTitle);
    }

    public static Topic createTopicRandom() {
        return GeneratorRandom.getRandomTopic();
    }

    public static Question createQuestionRandom(Type type) {
        Question question = new Question(getRandomQuestionText(), type);
        question.setPoint(getRandomPoint());
        return question;
    }

    public static AnswerOption createAnswerOptionRandom(Question question) {
        return new AnswerOption(
                getRandomString(10),
                getRandomBoolean(),
                getRandomString(8),
                question,
                new ArrayList<>()
        );
    }

    public static TestUpdateTestContentDto.AnswerOptionDto createAnswerOptionDtoNew(
    ) {
        return new TestUpdateTestContentDto.AnswerOptionDto(
                null,
                getRandomString(5),
                getRandomBoolean(),
                getRandomString(4)
        );
    }

    public static TestUpdateTestContentDto.AnswerOptionDto createAnswerOptionDtoExisting(
            Long idAnswerOption
    ) {
        return new TestUpdateTestContentDto.AnswerOptionDto(
                idAnswerOption,
                getRandomString(5),
                getRandomBoolean(),
                getRandomString(4)
        );
    }

    public static TestUpdateTestContentDto.QuestionDto createQuestionDtoNew(
            String textQuestion,
            Type typeQuestion,
            BigDecimal pointQuestion,
            List<String> correctTextAnswers,
            List<TestUpdateTestContentDto.AnswerOptionDto> answerOptionsDto
    ) {
        return new TestUpdateTestContentDto.QuestionDto(
                null,
                textQuestion,
                typeQuestion,
                pointQuestion,
                correctTextAnswers,
                answerOptionsDto
        );
    }

    public static TestUpdateTestContentDto.QuestionDto createQuestionDtoExisting(
            Long idQuestion,
            Type typeQuestion,
            List<String> correctTextAnswers,
            List<TestUpdateTestContentDto.AnswerOptionDto> answerOptionsDto
    ) {
        return new TestUpdateTestContentDto.QuestionDto(
                idQuestion,
                getRandomQuestionText(),
                typeQuestion,
                getRandomPoint(),
                correctTextAnswers,
                answerOptionsDto
        );
    }

    public static TestUpdateTestContentDto.QuestionDto createQuestionDtoOpenText(
            List<String> correctTextAnswers
    ) {
        return createQuestionDtoNew(
                getRandomQuestionText(),
                Type.OPEN_TEXT,
                getRandomPoint(),
                correctTextAnswers,
                null
        );
    }

    public static TestUpdateTestContentDto.QuestionDto createQuestionDtoSingleChoice(
            List<TestUpdateTestContentDto.AnswerOptionDto> answerOptionsDto
    ) {
        return createQuestionDtoNew(
                getRandomQuestionText(),
                Type.SINGLE_CHOICE,
                getRandomPoint(),
                null,
                answerOptionsDto
        );
    }

    public static TestUpdateTestContentDto.QuestionDto createQuestionDtoExistingMultipleChoice(
            Long idQuestion,
            List<TestUpdateTestContentDto.AnswerOptionDto> answerOptionsDto
    ) {
        return createQuestionDtoExisting(idQuestion, Type.MULTIPLE_CHOICE, null, answerOptionsDto);
    }

    /**
     * Создаёт UserAnswerRequestDto с выбранными вариантами ответов (для SINGLE_CHOICE / MULTIPLE_CHOICE вопросов).
     */
    public static ResultRequestDto.UserAnswerRequestDto createUserAnswerRequestDtoWithSelectedOptions(
            List<Long> idsSelectedAnswerOption
    ) {
        return new ResultRequestDto.UserAnswerRequestDto("", idsSelectedAnswerOption);
    }

    /**
     * Создаёт UserAnswerRequestDto с текстовым ответом (для OPEN_TEXT вопросов).
     */
    public static ResultRequestDto.UserAnswerRequestDto createUserAnswerRequestDtoWithTextAnswer(
            String textAnswerWritten
    ) {
        return new ResultRequestDto.UserAnswerRequestDto(textAnswerWritten, Collections.emptyList());
    }

    /**
     * Создаёт AnswerOption с заданными параметрами корректности.
     */
    public static AnswerOption createAnswerOption(String text, boolean isCorrect, Question question) {
        return new AnswerOption(text, isCorrect, null, question, new ArrayList<>());
    }

    /**
     * Создаёт Question типа SINGLE_CHOICE с заданным количеством баллов и привязкой к тесту.
     */
    public static Question createQuestionSingleChoice(String text, BigDecimal point, TestModel test) {
        Question question = new Question(text, Type.SINGLE_CHOICE);
        question.setPoint(point);
        question.setTest(test);
        return question;
    }

    /**
     * Создаёт Question типа MULTIPLE_CHOICE с возможностью допускать ошибки.
     */
    public static Question createQuestionMultipleChoiceWithAllowMistakes(
            String text,
            BigDecimal point,
            TestModel test
    ) {
        Question question = new Question(text, Type.MULTIPLE_CHOICE);
        question.setPoint(point);
        question.setAllowMistakes(true);
        question.setTest(test);
        return question;
    }

    /**
     * Создаёт Question типа OPEN_TEXT с заданными правильными ответами и привязкой к тесту.
     */
    public static Question createQuestionOpenText(
            String text,
            BigDecimal point,
            List<String> correctTextAnswers,
            TestModel test
    ) {
        Question question = new Question(text, Type.OPEN_TEXT);
        question.setPoint(point);
        question.setCorrectTextAnswers(correctTextAnswers);
        question.setTest(test);
        return question;
    }

    /**
     * Создаёт Result без grade и score (для тестирования compilateResultAsync).
     */
    public static Result createResultWithoutGrade(
            int timeSpentSeconds,
            User participant,
            TestModel test
    ) {
        return new Result(timeSpentSeconds, LocalDateTime.now(), participant, test);
    }

    /**
     * Создаёт Result с полными данными и привязкой к тесту.
     */
    public static Result createResultWithTest(
            User participant,
            TestModel test,
            Grade grade,
            BigDecimal score,
            int timeSpentSeconds
    ) {
        Result result = new Result(timeSpentSeconds, LocalDateTime.now(), participant, test);
        result.setGrade(grade);
        result.setScore(score);
        return result;
    }

    /**
     * Создаёт UserAnswer для вопроса с выбором ответа (SINGLE_CHOICE / MULTIPLE_CHOICE).
     */
    public static UserAnswer createUserAnswerForChoiceQuestion(
            Result result,
            Question question,
            AnswerOption selectedOption,
            boolean isCorrect
    ) {
        return new UserAnswer(isCorrect, result, question, selectedOption);
    }

    /**
     * Создаёт UserAnswer для вопроса с открытым текстом (OPEN_TEXT).
     */
    public static UserAnswer createUserAnswerForOpenTextQuestion(
            Result result,
            Question question,
            String textAnswer,
            boolean isCorrect
    ) {
        return new UserAnswer(textAnswer, isCorrect, result, question);
    }
}
