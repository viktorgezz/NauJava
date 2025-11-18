package ru.viktorgezz.NauJava.domain.test.service;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationPostgresTest;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.repo.TestRepo;
import ru.viktorgezz.NauJava.domain.test.service.intrf.TestQueryService;
import ru.viktorgezz.NauJava.domain.topic.Topic;
import ru.viktorgezz.NauJava.domain.topic.TopicRepo;
import ru.viktorgezz.NauJava.domain.topic.TopicService;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;
import ru.viktorgezz.NauJava.util.CreationModel;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.viktorgezz.NauJava.util.CreationModel.*;

@DisplayName("TestQueryService Integration Tests")
public class TestQueryServiceTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private TestQueryService testQueryService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private TestRepo testRepo;
    @Autowired
    private TopicRepo topicRepo;

    private TestModel testModel1;
    private User author;
    private Topic topic1;

    @BeforeEach
    void setup() {
        author = CreationModel.createRandomUser();
        userRepo.save(author);

        testModel1 = createTest("The test 1", "description 1", Status.PUBLIC, author);
        TestModel testModel2 = createTest("The test 2", "description 2", Status.PUBLIC, author);

        testRepo.saveAll(List.of(testModel1, testModel2));

        topic1 = createTopic("Topic 1");
        Topic topic2 = createTopic("Topic 2");
        Topic topicCommon = createTopic("Topic Common");

        topicRepo.saveAll(List.of(topic1, topic2, topicCommon));

        topicService.saveAndLinkAll(Set.of(topic1, topicCommon), testModel1);
        topicService.saveAndLinkAll(Set.of(topic2, topicCommon), testModel2);
    }

    @AfterEach
    void tearDown() {
        userRepo.deleteAll();
        testRepo.deleteAll();
        topicRepo.deleteAll();
    }

    @Test
    @DisplayName("Поиск всех Тестов")
    void shouldFindAllTests() {
        List<TestModel> tests = testQueryService.findAll();
        assertThat(tests)
                .hasSize(2)
                .extracting(TestModel::getTitle)
                .contains("The test 1", "The test 2");
    }

    @Test
    @DisplayName("Поиск теста по названию")
    void shouldFindAllTestsByTitle() {
        String testTitle = "The test 1";

        List<TestModel> tests = testQueryService.findAllByTitle(testTitle);
        assertThat(tests).hasSize(1);
        assertThat(tests.getFirst())
                .satisfies(t -> {
                    assertThat(t.getTitle()).isEqualTo(testTitle);
                    assertThat(t.getId()).isEqualTo(testModel1.getId());
                });

    }

    @Test
    @DisplayName("Поиск тестов по несуществующего названию")
    void shouldReturnEmptyList_WhenTitleUnknown() {
        String testTitle = "";

        List<TestModel> tests = testQueryService.findAllByTitle(testTitle);
        assertThat(tests).isEmpty();
    }

    @Test
    @DisplayName("Поиск нескольких тестов с одинаковым названием")
    void shouldFindAllTestsWithSameTitleByTitle() {
        String testTitle = "Duplicate Title";
        TestModel testNew1 = createTest(testTitle, "desc 1", Status.PUBLIC, author);
        TestModel testNew2 = createTest(testTitle, "desc 2", Status.PUBLIC, author);
        testRepo.save(testNew1);
        testRepo.save(testNew2);

        List<TestModel> testsFound = testQueryService.findAllByTitle(testTitle);
        assertThat(testsFound).hasSize(2);

        TestModel testFound1 = testsFound.getFirst();
        TestModel testFound2 = testsFound.getLast();

        assertThat(testFound1.getTitle()).isEqualTo(testFound2.getTitle());
        assertThat(testFound1.getId()).isNotEqualTo(testFound2.getId());
        assertThat(testFound1.getDescription()).isNotEqualTo(testFound2.getDescription());
    }

    @Test
    @DisplayName("Поиск тестов по списку названий тем")
    void shouldFindTestsByTopicTitles() {
        String topicTitle1 = "Topic 1";
        String topicTitle2 = "Topic Common";

        List<TestModel> testsFoundByTopicTitle1 = testQueryService.findTestsByTopicsTitle(List.of(topicTitle1));
        List<TestModel> testsFoundByTopicTitle2 = testQueryService.findTestsByTopicsTitle(List.of(topicTitle2));
        List<TestModel> testsFoundByTopicTitles = testQueryService.findTestsByTopicsTitle(List.of(topicTitle1, topicTitle2));
        List<TestModel> resEmpty = testQueryService.findTestsByTopicsTitle(List.of("Unknown Topic"));


        assertThat(testsFoundByTopicTitle1).hasSize(1)
                .extracting(TestModel::getTitle).contains("The test 1");
        assertThat(testsFoundByTopicTitle2).hasSize(2)
                .extracting(TestModel::getTitle)
                .contains("The test 1", "The test 2");
        assertThat(testsFoundByTopicTitles).hasSize(2);
        assertThat(resEmpty).isEmpty();
    }

    @Test
    @DisplayName("Поиск всех тестов с подгрузкой авторов и тем (FETCH JOIN)")
    void shouldFindAllTestsWithAuthorAndTopics() {
        List<TestModel> tests = testQueryService.findAllWithAuthorAndTopics();

        TestModel testFound = tests.stream()
                .filter(test -> test.getTitle().equals("The test 1"))
                .findFirst().orElseThrow();

        assertThat(tests)
                .extracting(TestModel::getTitle)
                .contains(testModel1.getTitle());
        assertThat(tests)
                .extracting(test -> test.getAuthor().getUsername())
                .contains(testModel1.getAuthor().getUsername());
        assertThat(testFound.getTestTopics())
                .extracting(testTopic -> testTopic.getTopic().getTitle())
                .contains(topic1.getTitle(), "Topic Common");
    }

}
