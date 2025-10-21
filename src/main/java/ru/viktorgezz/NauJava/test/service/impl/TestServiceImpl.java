package ru.viktorgezz.NauJava.test.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.NauJava.test.TestModel;
import ru.viktorgezz.NauJava.test.TestRequestDto;
import ru.viktorgezz.NauJava.test.repo.TestRepo;
import ru.viktorgezz.NauJava.test.service.intrf.TestService;
import ru.viktorgezz.NauJava.test_topic.TestTopic;
import ru.viktorgezz.NauJava.topic.Topic;
import ru.viktorgezz.NauJava.topic.TopicRepo;
import ru.viktorgezz.NauJava.user.User;
import ru.viktorgezz.NauJava.user.repo.UserRepo;


import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class TestServiceImpl implements TestService {

    private final TestRepo testRepo;
    private final UserRepo userRepo;
    private final TopicRepo topicRepo;

    @Autowired
    public TestServiceImpl(
            TestRepo testRepo,
            UserRepo userRepo,
            TopicRepo topicRepo
    ) {
        this.testRepo = testRepo;
        this.userRepo = userRepo;
        this.topicRepo = topicRepo;
    }

    @Override
    @Transactional
    public TestModel createTest(TestRequestDto createRequest) {

        Set<Long> topicIds = createRequest.topicIds();

        User author = userRepo.findById(createRequest.authorId())
                .orElseThrow(() -> new EntityNotFoundException("Author not found with id: " + createRequest.authorId()));

        Set<Topic> foundTopics = new HashSet<>();
        if (topicIds != null && !topicIds.isEmpty()) {
            Iterable<Topic> topicsIterable = topicRepo.findAllById(topicIds);
            Set<Topic> topicsSet = StreamSupport
                    .stream(
                            topicsIterable.spliterator(),
                            false
                    )
                    .collect(Collectors.toSet());
            foundTopics.addAll(topicsSet);

            if (foundTopics.size() != topicIds.size()) {
                throw new EntityNotFoundException("One or more topics not found for the given IDs.");
            }
        }

        TestModel newTest = new TestModel();
        newTest.setTitle(createRequest.title());
        newTest.setDescription(createRequest.description());
        newTest.setStatus(createRequest.status());
        newTest.setAuthor(author);

        createRequest.questions().forEach(q -> {
            q.setTest(newTest);
            newTest.getQuestions().add(q);
        });

        foundTopics.forEach(t -> {
            TestTopic testTopicLink = new TestTopic();
            testTopicLink.setTest(newTest);
            testTopicLink.setTopic(t);
            newTest.getTestTopics().add(testTopicLink);
        });

        return testRepo.save(newTest);
    }
}
