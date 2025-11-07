package ru.viktorgezz.NauJava.util;

import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.question.Type;
import ru.viktorgezz.NauJava.domain.report.StatusReport;
import ru.viktorgezz.NauJava.domain.report.ReportUserCountResultsModel;
import ru.viktorgezz.NauJava.domain.result.Grade;
import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.topic.Topic;
import ru.viktorgezz.NauJava.domain.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
            LocalDateTime completedAt,
            Long timeSpentSearchingForUsersMillis,
            Long timeSpentSearchingForResultsMillis,
            Long timeSpentCommonMillis
            ) {
        return new ReportUserCountResultsModel(
                status,
                countUsers,
                timeSpentSearchingForUsersMillis,
                timeSpentSearchingForResultsMillis,
                timeSpentCommonMillis,
                completedAt
                );
    }

    public static User createRandomUser() {
        return GeneratorRandomModel.getRandomUser();
    }

    public static Topic createTopic(String topicTitle) {
        return new Topic(topicTitle);
    }

    public static Question createQuestion(String text, Type type) {
        return new Question(text, type);
    }
}
