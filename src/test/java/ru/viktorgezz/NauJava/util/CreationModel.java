package ru.viktorgezz.NauJava.util;

import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.question.Type;
import ru.viktorgezz.NauJava.domain.report.StatusReport;
import ru.viktorgezz.NauJava.domain.report.model.ReportResultData;
import ru.viktorgezz.NauJava.domain.report.model.ReportUserCountResultsModel;
import ru.viktorgezz.NauJava.domain.report.dto.ResultResponse;
import ru.viktorgezz.NauJava.domain.result.Grade;
import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.topic.Topic;
import ru.viktorgezz.NauJava.domain.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

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
                new Random().nextInt()*15,
                LocalDateTime.now(),
                usernameParticipant,
                titleTest
                );
    }

    public static User createRandomUser() {
        return GeneratorRandomModel.getRandomUser();
    }

    public static Topic createTopic(String topicTitle) {
        return new Topic(topicTitle);
    }

    public static Topic createTopicRandom() {
        return GeneratorRandomModel.getRandomTopic();
    }

    public static Question createQuestion(String text, Type type) {
        return new Question(text, type);
    }
}
