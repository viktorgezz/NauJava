package ru.viktorgezz.NauJava.test.repo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.viktorgezz.NauJava.AbstractIntegrationPostgresTest;
import ru.viktorgezz.NauJava.test.Status;
import ru.viktorgezz.NauJava.test.TestModel;
import ru.viktorgezz.NauJava.test_topic.TestTopic;
import ru.viktorgezz.NauJava.test_topic.TestTopicRepo;
import ru.viktorgezz.NauJava.topic.Topic;
import ru.viktorgezz.NauJava.topic.TopicRepo;
import ru.viktorgezz.NauJava.user.User;
import ru.viktorgezz.NauJava.user.repo.UserRepo;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.viktorgezz.NauJava.util.CreationModel.createRandomUser;
import static ru.viktorgezz.NauJava.util.CreationModel.createTest;
import static ru.viktorgezz.NauJava.util.CreationModel.createTopic;

@DisplayName("TestRepo Integration Test")
class TestRepoTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private TestRepo testRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TopicRepo topicRepo;

    @Autowired
    private TestTopicRepo testTopicRepo;

    private User author1;
    private Topic topicJava;
    private Topic topicSql;
    private Topic topicSpring;

    @BeforeEach
    void setUp() {
        author1 = userRepo.save(createRandomUser());

        topicJava = topicRepo.save(createTopic("Java Basics"));
        topicSql = topicRepo.save(createTopic("SQL"));
        topicSpring = topicRepo.save(createTopic("Spring Framework"));
    }

    @AfterEach
    void tearDown() {
        testTopicRepo.deleteAll();
        testRepo.deleteAll();
        topicRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("Поиск тестов по точному названию")
    void findByTitle_shouldReturnMatchingTests() {
        // Arrange
        String targetTitle = "Java Core Test";
        testRepo.save(createTest(targetTitle, "Test on core Java concepts", Status.PUBLIC, author1));
        testRepo.save(createTest("Spring Boot Basics", "Intro to Spring Boot", Status.UNLISTED, author1));
        testRepo.save(createTest(targetTitle, "Another test with the same title", Status.PUBLIC, author1));

        // Act
        List<TestModel> foundTests = testRepo.findByTitle(targetTitle);

        // Assert
        assertThat(foundTests).hasSize(2);
        assertThat(foundTests).allMatch(test -> test.getTitle().equals(targetTitle));
    }

    @Test
    @DisplayName("Возвращение пустого списка, если тесты не найдены")
    void findByTitle_shouldReturnEmptyListForNonMatchingTitle() {
        // Arrange
        testRepo.save(createTest("Java Core Test", "Test on core Java concepts", Status.PUBLIC, author1));

        // Act
        List<TestModel> foundTests = testRepo.findByTitle("NonExistentTitle");

        // Assert
        assertThat(foundTests).isEmpty();
    }


    @Test
    @DisplayName("Поиск тестов, связанных хотя бы с одной из тем по названию")
    void findTestsByTopicTitles_shouldReturnAssociatedTests() {
        // Arrange
        TestModel javaTest = testRepo.save(createTest("Java Advanced", "Advanced Java topics", Status.PUBLIC, author1));
        TestModel springTest = testRepo.save(createTest("Spring Test", "Spring topics", Status.PRIVATE, author1));
        TestModel sqlTest = testRepo.save(createTest("SQL Basics", "Basic SQL queries", Status.PUBLIC, author1));
        TestModel combinedTest = testRepo.save(createTest("Java & Spring", "Combined topics", Status.PUBLIC, author1));

        linkTestTopic(javaTest, topicJava);
        linkTestTopic(springTest, topicSpring);
        linkTestTopic(sqlTest, topicSql);
        linkTestTopic(combinedTest, topicJava);
        linkTestTopic(combinedTest, topicSpring);

        List<String> searchTopicTitles = List.of("Java Basics", "SQL");

        // Act
        List<TestModel> foundTests = testRepo.findTestsByTopicTitles(searchTopicTitles);

        // Assert
        assertThat(foundTests).hasSize(3);
        assertThat(foundTests)
                .extracting(TestModel::getTitle)
                .containsExactlyInAnyOrder("Java Advanced", "SQL Basics", "Java & Spring");
        assertThat(foundTests).noneMatch(test -> test.getTitle().equals("Spring Test"));
    }

    @Test
    @DisplayName("Поиск уникальных тестов, даже если у теста несколько искомых тем")
    void findTestsByTopicTitles_shouldReturnDistinctTests() {
        // Arrange
        TestModel combinedTest = testRepo.save(createTest("Java & Spring", "Combined topics", Status.PUBLIC, author1));
        linkTestTopic(combinedTest, topicJava);
        linkTestTopic(combinedTest, topicSpring);

        List<String> searchTopicTitles = List.of("Java Basics", "Spring Framework");

        // Act
        List<TestModel> foundTests = testRepo.findTestsByTopicTitles(searchTopicTitles);

        // Assert
        assertThat(foundTests).hasSize(1);
        assertThat(foundTests.getFirst().getTitle()).isEqualTo("Java & Spring");
    }


    @Test
    @DisplayName("Возвращение пустого списка для тем без тестов")
    void findTestsByTopicTitles_shouldReturnEmptyListForTopicsWithNoTests() {
        // Arrange
        Topic unusedTopic = new Topic();
        unusedTopic.setTitle("Unused");
        topicRepo.save(unusedTopic);

        List<String> searchTopicTitles = List.of("Unused");

        testRepo.save(createTest("Some Test", "Desc", Status.PUBLIC, author1));

        // Act
        List<TestModel> foundTests = testRepo.findTestsByTopicTitles(searchTopicTitles);

        // Assert
        assertThat(foundTests).isEmpty();
    }

    @Test
    @DisplayName("Возвращение пустого списка для несуществующих названий тем")
    void findTestsByTopicTitles_shouldReturnEmptyListForNonExistentTopicTitles() {
        // Arrange
        List<String> nonExistentTopicTitles = List.of("NonExistentTopic1", "NonExistentTopic2");
        testRepo.save(createTest("Some Test", "Desc", Status.PUBLIC, author1));

        // Act
        List<TestModel> foundTests = testRepo.findTestsByTopicTitles(nonExistentTopicTitles);

        // Assert
        assertThat(foundTests).isEmpty();
    }

    @Test
    @DisplayName("Возвращение пустого списка при передаче пустого списка названий")
    void findTestsByTopicTitles_shouldReturnEmptyListForEmptyInputList() {
        // Arrange
        List<String> emptyList = Collections.emptyList();
        testRepo.save(createTest("Some Test", "Desc", Status.PUBLIC, author1));

        // Act
        List<TestModel> foundTests = testRepo.findTestsByTopicTitles(emptyList);

        // Assert
        assertThat(foundTests).isEmpty();
    }

    @Test
    @DisplayName("Получение всех тестов с подгруженными автором и темами")
    void findAllWithAuthorAndTopics_shouldReturnAllWithRelations() {
        // Arrange
        User author2 = userRepo.save(createRandomUser());

        TestModel testModel1 = testRepo.save(createTest("T1", "desc", Status.PUBLIC, author1));
        TestModel testModel2 = testRepo.save(createTest("T2", "desc", Status.PUBLIC, author2));

        linkTestTopic(testModel1, topicJava);
        linkTestTopic(testModel1, topicSpring);
        linkTestTopic(testModel2, topicSql);

        // Act
        List<TestModel> tests = testRepo.findAllWithAuthorAndTopics();

        // Assert
        assertThat(tests).hasSize(2);
        assertThat(tests)
                .extracting(TestModel::getTitle)
                .containsExactlyInAnyOrder("T1", "T2");

        TestModel loadedTest1 = tests.stream().filter(t -> t.getTitle().equals("T1")).findFirst().orElseThrow();
        TestModel loadedTest2 = tests.stream().filter(t -> t.getTitle().equals("T2")).findFirst().orElseThrow();

        assertThat(loadedTest1.getAuthor().getId()).isEqualTo(author1.getId());
        assertThat(loadedTest2.getAuthor().getId()).isEqualTo(author2.getId());

        assertThat(loadedTest1.getTestTopics())
                .extracting(testTopic -> testTopic.getTopic().getTitle())
                .containsExactlyInAnyOrder("Java Basics", "Spring Framework");

        assertThat(loadedTest2.getTestTopics())
                .extracting(testTopic -> testTopic.getTopic().getTitle())
                .containsExactlyInAnyOrder("SQL");
    }

    private void linkTestTopic(TestModel test, Topic topic) {
        TestTopic link = new TestTopic();
        link.setTest(test);
        link.setTopic(topic);
        testTopicRepo.save(link);
    }
}