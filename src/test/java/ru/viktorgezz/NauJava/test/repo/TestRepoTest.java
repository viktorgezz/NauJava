package ru.viktorgezz.NauJava.test.repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import ru.viktorgezz.NauJava.AbstractIntegrationPostgresTest;
import ru.viktorgezz.NauJava.test.Status;
import ru.viktorgezz.NauJava.test.TestModel;
import ru.viktorgezz.NauJava.test_topic.TestTopic;
import ru.viktorgezz.NauJava.test_topic.TestTopicRepo;
import ru.viktorgezz.NauJava.topic.Topic;
import ru.viktorgezz.NauJava.topic.TopicRepo;
import ru.viktorgezz.NauJava.user.User;
import ru.viktorgezz.NauJava.user.repo.UserRepo;
import ru.viktorgezz.NauJava.util.SavingModel;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
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

    @Autowired
    private SavingModel savingModel;

    private User author1;
    private Topic topicJava;
    private Topic topicSql;
    private Topic topicSpring;

    @BeforeEach
    void setUp() {
        testTopicRepo.deleteAll();
        testRepo.deleteAll();
        topicRepo.deleteAll();
        userRepo.deleteAll();

        author1 = savingModel.createAndSaveRandomUser();

        topicJava = savingModel.createAndSaveTopic("Java Basics");
        topicSql = savingModel.createAndSaveTopic("SQL");
        topicSpring = savingModel.createAndSaveTopic("Spring Framework");
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


    private TestModel createTest(
            String title,
            String description,
            Status status,
            User author
    ) {
        TestModel testModel = new TestModel();
        testModel.setTitle(title);
        testModel.setDescription(description);
        testModel.setStatus(status);
        testModel.setAuthor(author);
        return testModel;
    }

    private void linkTestTopic(TestModel test, Topic topic) {
        TestTopic link = new TestTopic();
        link.setTest(test);
        link.setTopic(topic);
        testTopicRepo.save(link);
    }
}