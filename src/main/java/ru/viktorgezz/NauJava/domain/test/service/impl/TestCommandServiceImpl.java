package ru.viktorgezz.NauJava.domain.test.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.dto.TestRequestDto;
import ru.viktorgezz.NauJava.domain.test.dto.TestRequestThymeleafDto;
import ru.viktorgezz.NauJava.domain.test.repo.TestRepo;
import ru.viktorgezz.NauJava.domain.test.service.intrf.TestCommandService;
import ru.viktorgezz.NauJava.domain.topic.Topic;
import ru.viktorgezz.NauJava.domain.topic.TopicService;
import ru.viktorgezz.NauJava.domain.user.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для управления тестами. Реализует {@link TestCommandService}.
 */
@Service
public class TestCommandServiceImpl implements TestCommandService {

    private final TestRepo testRepo;

    private final TopicService topicService;

    @Autowired
    public TestCommandServiceImpl(
            TestRepo testRepo,
            TopicService topicService
    ) {
        this.testRepo = testRepo;
        this.topicService = topicService;
    }

    @Override
    @Transactional
    public TestModel createTest(
            TestRequestDto createRequest,
            User author
    ) {
        return internalCreateTest(
                createRequest,
                author
        );
    }

    @Override
    @Transactional
    public TestModel createTestThymeleaf(
            TestRequestThymeleafDto testDto,
            User author
    ) {
        return internalCreateTest(
                new TestRequestDto(
                        testDto.getTitle(),
                        testDto.getDescription(),
                        Status.valueOf(testDto.getStatusParam()),
                        new ArrayList<>(),
                        resolveTopicIds(testDto)),
                author
        );
    }

    private TestModel internalCreateTest(
            TestRequestDto createRequest,
            User author
    ) {
        Set<Long> idsTopic = createRequest.topicIds();

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
}