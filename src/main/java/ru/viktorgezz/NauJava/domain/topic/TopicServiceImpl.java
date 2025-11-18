package ru.viktorgezz.NauJava.domain.topic;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.many_to_many_entity.test_topic.TestTopic;
import ru.viktorgezz.NauJava.domain.test.repo.TestRepo;

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
    public List<Topic> findAll() {
        return topicRepo.findAll();
    }

    public Set<Topic> findAllByIds(Set<Long> idsTopic) {
        return StreamSupport.stream(
                        topicRepo
                                .findAllById(idsTopic).spliterator(), false)
                .collect(Collectors.toSet());
    }

    @Transactional
    public Topic save(Topic topic) {
        return topicRepo.save(topic);
    }

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
