package ru.viktorgezz.NauJava.test.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.NauJava.test.Status;
import ru.viktorgezz.NauJava.test.TestModel;
import ru.viktorgezz.NauJava.test.dto.TestRequestDto;
import ru.viktorgezz.NauJava.test.dto.TestRequestThymeleafDto;
import ru.viktorgezz.NauJava.test.repo.TestRepo;
import ru.viktorgezz.NauJava.test.service.intrf.TestCommandService;
import ru.viktorgezz.NauJava.topic.Topic;
import ru.viktorgezz.NauJava.topic.TopicService;
import ru.viktorgezz.NauJava.user.User;
import ru.viktorgezz.NauJava.user.repo.UserRepo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для управления тестами. Реализует {@link ru.viktorgezz.NauJava.test.service.intrf.TestCommandService}.
 */
@Service
public class TestCommandServiceImpl implements TestCommandService {

    private final TestRepo testRepo;
    private final UserRepo userRepo;

    private final TopicService topicService;

    @Autowired
    public TestCommandServiceImpl(
            TestRepo testRepo,
            UserRepo userRepo,
            TopicService topicService
    ) {
        this.testRepo = testRepo;
        this.userRepo = userRepo;
        this.topicService = topicService;
    }

    @Override
    @Transactional
    public TestModel createTest(TestRequestDto createRequest) {
        return internalCreateTest(createRequest);
    }

    @Override
    @Transactional
    public TestModel createTestThymeleaf(TestRequestThymeleafDto testDto) {
        return internalCreateTest(new TestRequestDto(
                testDto.getTitle(),
                testDto.getDescription(),
                Status.valueOf(testDto.getStatusParam()),
                -1L, // временная мера для демонстрации работы на страницу new. (Thymeleaf)
                new ArrayList<>(),
                resolveTopicIds(testDto)
        ));
    }

    private Set<Long> resolveTopicIds(TestRequestThymeleafDto testDto) {
        Set<Long> topicIds = new HashSet<>();
        if (testDto.getSelectedTopicIds() != null && testDto.getSelectedTopicIds().length != 0) {
            topicIds = Arrays
                    .stream(testDto.getSelectedTopicIds())
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
        }

        if (testDto.getNewTopicTitle() != null && !testDto.getNewTopicTitle().trim().isEmpty()) {
            Topic newTopic = new Topic(testDto.getNewTopicTitle());
            newTopic = topicService.save(newTopic);
            topicIds.add(newTopic.getId());
        }
        return topicIds;
    }

    private TestModel internalCreateTest(TestRequestDto createRequest) {
        Set<Long> idsTopic = createRequest.topicIds();

        User author = userRepo.findById(createRequest.authorId()).orElse(null); // временная мера для демонстрации работы на страницу new. (Thymeleaf)

        Set<Topic> foundTopics = topicService.findAllById(idsTopic);

        TestModel newTest = new TestModel(
                createRequest.title(),
                createRequest.description(),
                createRequest.status(),
                author
        );

        createRequest.questions().forEach(question -> {
            question.setTest(newTest);
            newTest.getQuestions().add(question);
        });

        topicService.saveAndLinkAll(foundTopics, newTest);

        return testRepo.save(newTest);
    }
}