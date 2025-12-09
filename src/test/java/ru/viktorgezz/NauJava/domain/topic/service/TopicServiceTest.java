package ru.viktorgezz.NauJava.domain.topic.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.viktorgezz.NauJava.domain.many_to_many_entity.test_topic.TestTopic;
import ru.viktorgezz.NauJava.domain.many_to_many_entity.test_topic.TestTopicRepo;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.repo.TestRepo;
import ru.viktorgezz.NauJava.domain.topic.Topic;
import ru.viktorgezz.NauJava.domain.topic.repo.TopicRepo;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationPostgresTest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.viktorgezz.NauJava.util.CreationModel.*;

@DisplayName("TopicServiceTest Integration Tests")
public class TopicServiceTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private TopicService topicService;

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private TopicRepo topicRepo;
    @Autowired
    private TestRepo testRepo;
    @Autowired
    private TestTopicRepo testTopicRepo;

    private Topic topic1;
    private Topic topic2;

    private TestModel test;


    @BeforeEach
    void setUp() {
        topic1 = createTopic("Topic 1");
        topic2 = createTopic("Topic 2");
        topicRepo.saveAll(List.of(topic1, topic2));

        User author = createUserRandom();
        userRepo.save(author);

        test = createTest("The test 1", "desc 1", Status.PRIVATE, author);
        testRepo.save(test);
    }

    @AfterEach
    void tearDown() {
        testTopicRepo.deleteAll();
        userRepo.deleteAll();
        topicRepo.deleteAll();
        testRepo.deleteAll();
    }

    @Test
    @DisplayName("saveAndLinkAll: связывает темы с тестом и сохраняет их")
    void saveAndLinkAll_ShouldLinkTopicsWithTest_WhenTopicsProvided() {
        Set<Topic> topics = Set.of(topic1, topic2);

        topicService.saveAndLinkAll(topics, test);

        List<TestModel> tests = testRepo.findAllWithAuthorAndTopics();
        assertThat(tests).hasSize(1);
        TestModel testFound = tests.getFirst();

        assertThat(topicRepo.findAllByIdTestModel(testFound.getId()))
                .containsOnly(topic1, topic2)
                .flatExtracting(topic -> topic.getTestTopics().stream()
                        .map(TestTopic::getTest)
                        .collect(Collectors.toSet()))
                .containsOnly(testFound);
    }

    @Test
    @DisplayName("findOrCreateTopics: возвращает существующие и создаёт новые темы по названиям")
    void findOrCreateTopics_ShouldReturnExistingAndCreateMissing_WhenTitlesMixed() {
        topicRepo.save(createTopic("existing"));
        List<String> titlesRequested = List.of("existing", "NEW", "new");

        List<Topic> topicsResult = topicService.findOrCreateTopics(titlesRequested);

        assertThat(topicsResult).hasSize(2);
        Set<String> titlesResult = topicsResult.stream()
                .map(Topic::getTitle)
                .collect(Collectors.toSet());
        assertThat(titlesResult).containsExactlyInAnyOrder("existing", "new");
    }

    @Test
    @DisplayName("saveAll: сохраняет все переданные темы")
    void saveAll_ShouldPersistAllTopics_WhenIterableProvided() {
        topicRepo.deleteAll();
        Topic topicFirst = createTopic("bulk1");
        Topic topicSecond = createTopic("bulk2");

        topicService.saveAll(List.of(topicFirst, topicSecond));

        List<Topic> topicsFound = topicRepo.findAll();
        assertThat(topicsFound)
                .extracting(Topic::getTitle)
                .containsExactlyInAnyOrder("bulk1", "bulk2");
    }
}
