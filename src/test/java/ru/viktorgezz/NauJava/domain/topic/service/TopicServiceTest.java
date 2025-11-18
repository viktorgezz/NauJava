package ru.viktorgezz.NauJava.domain.topic.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationPostgresTest;
import ru.viktorgezz.NauJava.domain.many_to_many_entity.test_topic.TestTopic;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.repo.TestRepo;
import ru.viktorgezz.NauJava.domain.topic.Topic;
import ru.viktorgezz.NauJava.domain.topic.TopicRepo;
import ru.viktorgezz.NauJava.domain.topic.TopicService;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;

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

    private Topic topic1;
    private Topic topic2;

    private TestModel test;


    @BeforeEach
    void setUp() {
        topic1 = createTopic("Topic 1");
        topic2 = createTopic("Topic 2");
        topicRepo.saveAll(List.of(topic1, topic2));

        User author = createRandomUser();
        userRepo.save(author);

        test = createTest("The test 1", "desc 1", Status.PRIVATE, author);
        testRepo.save(test);
    }

    @AfterEach
    void tearDown() {
        userRepo.deleteAll();
        topicRepo.deleteAll();
        testRepo.deleteAll();
    }

    @Test
    @DisplayName("Поиск всех тем")
    void shouldFindAll() {
        List<Topic> topics = topicService.findAll();

        assertThat(topics).hasSize(2);
        assertThat(topics).extracting(Topic::getTitle).containsOnly("Topic 1", "Topic 2");
    }

    @Test
    @DisplayName("Поиск тем по их уникальному набору id")
    void shouldFindAllByIdsExist() {
        Set<Long> ids = Set.of(topic1.getId(), topic2.getId());
        Set<Topic> topics = topicService.findAllByIds(ids);

        assertThat(topics).hasSize(2);
        assertThat(topics).extracting(Topic::getTitle).containsOnly("Topic 1", "Topic 2");
    }

    @Test
    @DisplayName("Поиск тем по их пустому набору id")
    void shouldReturnEmptySet_WhenIdsEmpty() {
        Set<Long> ids = Set.of();
        Set<Topic> topics = topicService.findAllByIds(ids);

        assertThat(topics).hasSize(0);
        assertThat(topics).extracting(Topic::getTitle).doesNotContain("Topic 1", "Topic 2");
    }

    @Test
    @DisplayName("Поиск тем по частичному совпадению")
    void findAllByIds_WhenSomeIdsDoNotExist_ShouldReturnOnlyExisting() {
        Long existingId = topic1.getId();
        Long nonExistentId = 999999L;
        Set<Long> requestedIds = Set.of(existingId, nonExistentId);

        Set<Topic> foundTopics = topicService.findAllByIds(requestedIds);

        assertThat(foundTopics)
                .hasSize(1)
                .extracting(Topic::getId)
                .containsOnly(existingId)
                .doesNotContain(nonExistentId);
    }

    @Test
    @DisplayName("Сохранение и связывание тем с тестом")
    void shouldSaveAndLinkTestWithTestModel() {
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
    @DisplayName("Сохранение темы")
    void shouldSaveTopic() {
        Topic topic = createTopic("Save Topic");
        topic = topicService.save(topic);

        assertThat(topic.getId()).isNotNull();
        assertThat(topicRepo.findAll()).extracting(Topic::getTitle).contains("Save Topic");
    }

}
