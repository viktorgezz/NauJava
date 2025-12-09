package ru.viktorgezz.NauJava.domain.topic.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.domain.many_to_many_entity.test_topic.TestTopic;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.repo.TestRepo;
import ru.viktorgezz.NauJava.domain.topic.Topic;
import ru.viktorgezz.NauJava.domain.topic.repo.TopicRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Реализация сервиса тем. Реализует {@link TopicService}.
 */
@Service
public class TopicServiceImpl implements TopicService {

    private final TopicRepo topicRepo;
    private final TestRepo testRepo;

    @Autowired
    public TopicServiceImpl(
            TopicRepo topicRepo,
            TestRepo testRepo
    ) {
        this.topicRepo = topicRepo;
        this.testRepo = testRepo;
    }

    @Override
    @Transactional
    public List<Topic> findOrCreateTopics(List<String> titlesTopic) {
        List<Topic> topics = new ArrayList<>();
        titlesTopic
                .stream()
                .map(title -> title.trim().toLowerCase())
                .distinct()
                .forEach(title -> {
                    Optional<Topic> topicOptional = topicRepo.findByTitle(title.trim().toLowerCase());
                    topics.add(topicOptional.orElse(new Topic(title)));
                });
        return StreamSupport.stream(topicRepo.saveAll(topics).spliterator(), false).toList();
    }

    @Override
    @Transactional
    public void saveAll(Iterable<Topic> topics) {
        topicRepo.saveAll(topics);
    }

    @Override
    @Transactional
    public void saveAndLinkAll(
            Set<Topic> topics,
            TestModel testNew
    ) {
        topicRepo.saveAll(topics);

        Set<Long> idsTopicExistingLinked = testNew.getTestTopics().stream()
                .map(testTopic -> testTopic.getTopic().getId())
                .collect(Collectors.toSet());

        topics.forEach(topic -> {
            if (!idsTopicExistingLinked.contains(topic.getId())) {
                TestTopic testTopicLink = new TestTopic();
                testTopicLink.setTest(testNew);
                testTopicLink.setTopic(topic);
                testNew.getTestTopics().add(testTopicLink);
            }
        });

        topicRepo.saveAll(topics);
        testRepo.save(testNew);
    }

}
