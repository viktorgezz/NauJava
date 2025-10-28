package ru.viktorgezz.NauJava.topic;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.test.TestModel;
import ru.viktorgezz.NauJava.test_topic.TestTopic;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Реализация сервиса тем. Реализует {@link TopicService}.
 */
@Service
public class TopicServiceImpl implements TopicService {

    private final TopicRepo topicRepo;

    @Autowired
    public TopicServiceImpl(TopicRepo topicRepo) {
        this.topicRepo = topicRepo;
    }

    @Override
    public List<Topic> findAll() {
        return StreamSupport
                .stream(
                        topicRepo
                                .findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    public Set<Topic> findAllById(Set<Long> idsTopic) {
        return StreamSupport.stream(
                        topicRepo
                                .findAllById(idsTopic).spliterator(), false)
                .collect(Collectors.toSet());
    }

    @Transactional
    public void saveAndLinkAll(
            Set<Topic> foundTopics,
            TestModel newTest
    ) {
        foundTopics.forEach(t -> {
            TestTopic testTopicLink = new TestTopic();
            testTopicLink.setTest(newTest);
            testTopicLink.setTopic(t);
            newTest.getTestTopics().add(testTopicLink);
        });
    }

    @Transactional
    public Topic save(Topic topic) {
        return topicRepo.save(topic);
    }

}
