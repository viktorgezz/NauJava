package ru.viktorgezz.NauJava.domain.topic.repo;

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
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationPostgresTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.viktorgezz.NauJava.util.CreationModel.*;

@DisplayName("TopicRepo Integration Tests")
class TopicRepoTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private TopicRepo topicRepo;

    @Autowired
    private TestRepo testRepo;

    @Autowired
    private TestTopicRepo testTopicRepo;

    @Autowired
    private UserRepo userRepo;

    private User userAuthor;

    @BeforeEach
    void setUp() {
        userAuthor = userRepo.save(createUserRandom());
    }

    @AfterEach
    void tearDown() {
        testTopicRepo.deleteAll();
        testRepo.deleteAll();
        topicRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("findAll: возврат всех тем")
    void findAll_ShouldReturnAllTopics_WhenTopicsExist() {
        Topic topicFirst = topicRepo.save(createTopicRandom());
        Topic topicSecond = topicRepo.save(createTopicRandom());

        List<Topic> topicsFound = topicRepo.findAll();

        assertThat(topicsFound).hasSize(2);
        assertThat(topicsFound).extracting(Topic::getTitle)
                .containsExactlyInAnyOrder(topicFirst.getTitle(), topicSecond.getTitle());
    }

    @Test
    @DisplayName("findByTitle: возврат темы по названию")
    void findByTitle_ShouldReturnTopic_WhenTitleExists() {
        Topic topicSaved = topicRepo.save(createTopicRandom());

        Optional<Topic> topicFoundOptional = topicRepo.findByTitle(topicSaved.getTitle());

        assertThat(topicFoundOptional).isPresent();
        assertThat(topicFoundOptional.get().getTitle()).isEqualTo(topicSaved.getTitle());
    }

    @Test
    @DisplayName("findAllByIdTestModel: возврат тем, связанных с тестом")
    void findAllByIdTestModel_ShouldReturnTopicsLinkedToTest_WhenRelationsExist() {
        Topic topicLinkedFirst = topicRepo.save(createTopicRandom());
        Topic topicLinkedSecond = topicRepo.save(createTopicRandom());
        topicRepo.save(createTopicRandom());

        TestModel testModel = testRepo.save(createTest("Title", "Desc", Status.PUBLIC, userAuthor));

        TestTopic linkFirst = new TestTopic();
        linkFirst.setTest(testModel);
        linkFirst.setTopic(topicLinkedFirst);

        TestTopic linkSecond = new TestTopic();
        linkSecond.setTest(testModel);
        linkSecond.setTopic(topicLinkedSecond);

        testTopicRepo.saveAll(List.of(linkFirst, linkSecond));

        List<Topic> topicsFound = topicRepo.findAllByIdTestModel(testModel.getId());

        assertThat(topicsFound).hasSize(2);
        assertThat(topicsFound).extracting(Topic::getId)
                .containsExactlyInAnyOrder(topicLinkedFirst.getId(), topicLinkedSecond.getId());
    }
}
