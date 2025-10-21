package ru.viktorgezz.NauJava.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.viktorgezz.NauJava.result.Grade;
import ru.viktorgezz.NauJava.result.Result;
import ru.viktorgezz.NauJava.test.Status;
import ru.viktorgezz.NauJava.test.TestModel;
import ru.viktorgezz.NauJava.topic.Topic;
import ru.viktorgezz.NauJava.topic.TopicRepo;
import ru.viktorgezz.NauJava.user.User;
import ru.viktorgezz.NauJava.user.repo.UserRepo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class SavingModel {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TopicRepo topicRepo;

    public User createAndSaveRandomUser() {
        return userRepo.save(GeneratorRandomModel.getRandomUser());
    }

    public Result createResult(User user, Grade grade, BigDecimal score) {
        Result result = new Result();
        result.setParticipant(user);
        result.setGrade(grade);
        result.setScore(score);
        result.setCompletedAt(LocalDateTime.now());
        return result;
    }

    public TestModel createTest(
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

    public Topic createAndSaveTopic(String topicTitle) {
        return topicRepo.save(new Topic(topicTitle));
    }
}
