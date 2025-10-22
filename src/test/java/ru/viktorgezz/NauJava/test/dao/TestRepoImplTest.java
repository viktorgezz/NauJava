package ru.viktorgezz.NauJava.test.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import ru.viktorgezz.NauJava.AbstractIntegrationPostgresTest;
import ru.viktorgezz.NauJava.test.Status;
import ru.viktorgezz.NauJava.test.TestModel;
import ru.viktorgezz.NauJava.test.repo.TestRepo;
import ru.viktorgezz.NauJava.test_topic.TestTopic;
import ru.viktorgezz.NauJava.test_topic.TestTopicRepo;
import ru.viktorgezz.NauJava.topic.Topic;
import ru.viktorgezz.NauJava.topic.TopicRepo;
import ru.viktorgezz.NauJava.user.User;
import ru.viktorgezz.NauJava.user.repo.UserRepo;
import ru.viktorgezz.NauJava.util.GeneratorRandomModel;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DisplayName("TestRepoImpl Criteria API Tests")
class TestRepoImplTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private TestRepoImpl testRepoImpl;

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
    private TestModel javaTest;
    private TestModel springTest;
    private TestModel sqlTest;
    private TestModel combinedTest;

    @BeforeEach
    void setUp() {
        testTopicRepo.deleteAll();
        testRepo.deleteAll();
        topicRepo.deleteAll();
        userRepo.deleteAll();

        author1 = createAndSaveRandomUser();

        topicJava = createAndSaveTopic("Java Basics");
        topicSql = createAndSaveTopic("SQL");
        topicSpring = createAndSaveTopic("Spring Framework");

        javaTest = testRepo.save(createTest("Java Advanced", "Advanced Java topics", Status.PUBLIC, author1));
        springTest = testRepo.save(createTest("Spring Test", "Spring topics", Status.PRIVATE, author1));
        sqlTest = testRepo.save(createTest("SQL Basics", "Basic SQL queries", Status.PUBLIC, author1));
        combinedTest = testRepo.save(createTest("Java & Spring", "Combined topics", Status.PUBLIC, author1));

        linkTestTopic(javaTest, topicJava);
        linkTestTopic(springTest, topicSpring);
        linkTestTopic(sqlTest, topicSql);
        linkTestTopic(combinedTest, topicJava);
        linkTestTopic(combinedTest, topicSpring);
    }


    @Test
    @DisplayName("Поиск тестов по точному названию")
    void findByTitle_Criteria_shouldReturnMatchingTests() {
        // Arrange
        String targetTitle = "Java Advanced";
        testRepo.save(createTest(targetTitle, "Another one", Status.UNLISTED, author1)); // Добавим еще один с тем же названием

        // Act
        List<TestModel> foundTests = testRepoImpl.findByTitle(targetTitle);

        // Assert
        assertThat(foundTests).hasSize(2);
        assertThat(foundTests).allMatch(test -> test.getTitle().equals(targetTitle));
    }

    @Test
    @DisplayName("Возвращение пустого списка, если тесты не найдены")
    void findByTitle_Criteria_shouldReturnEmptyListForNonMatchingTitle() {
        // Act
        List<TestModel> foundTests = testRepoImpl.findByTitle("NonExistentTitle");

        // Assert
        assertThat(foundTests).isEmpty();
    }

    @Test
    @DisplayName("Результаты Criteria API и Derived Query должны совпадать по поиску тестов по точному названию")
    void findByTitle_CriteriaAndDerivedShouldMatch() {
        // Arrange
        String targetTitle = "Java Advanced";
        testRepo.save(createTest(targetTitle, "Another one", Status.UNLISTED, author1));

        // Act
        List<TestModel> resultsFromImpl = testRepoImpl.findByTitle(targetTitle);
        List<TestModel> resultsFromRepo = testRepo.findByTitle(targetTitle);

        // Assert
        assertThat(resultsFromImpl).hasSize(2);
        assertThat(resultsFromRepo).hasSize(2);
        assertThat(resultsFromImpl).containsExactlyInAnyOrderElementsOf(resultsFromRepo);
    }


    @Test
    @DisplayName("Поиск тестов по одному названию темы")
    void findTestsByTopicTitles_Criteria_shouldReturnTestsForSingleTitle() {
        // Arrange
        List<String> searchTitles = List.of("Java Basics");

        // Act
        List<TestModel> foundTests = testRepoImpl.findTestsByTopicTitles(searchTitles);

        // Assert
        assertThat(foundTests).hasSize(2);
        assertThat(foundTests)
                .extracting(TestModel::getTitle)
                .containsExactlyInAnyOrder("Java Advanced", "Java & Spring");
    }

    @Test
    @DisplayName("Поиск тестов по нескольким названиям тем")
    void findTestsByTopicTitles_Criteria_shouldReturnTestsForMultipleTitles() {
        // Arrange
        List<String> searchTitles = List.of("Java Basics", "SQL");

        // Act
        List<TestModel> foundTests = testRepoImpl.findTestsByTopicTitles(searchTitles);

        // Assert
        assertThat(foundTests).hasSize(3);
        assertThat(foundTests)
                .extracting(TestModel::getTitle)
                .containsExactlyInAnyOrder("Java Advanced", "SQL Basics", "Java & Spring");
        assertThat(foundTests).noneMatch(test -> test.getTitle().equals("Spring Test"));
    }

    @Test
    @DisplayName("Поиск уникальных тестов по нескольким названиям тем")
    void findTestsByTopicTitles_Criteria_shouldReturnDistinctTests() {
        // Arrange
        List<String> searchTitles = List.of("Java Basics", "Spring Framework");

        // Act
        List<TestModel> foundTests = testRepoImpl.findTestsByTopicTitles(searchTitles);

        // Assert
        assertThat(foundTests).hasSize(3);
        assertThat(foundTests)
                .extracting(TestModel::getTitle)
                .containsExactlyInAnyOrder("Java Advanced", "Spring Test", "Java & Spring");
        assertThat(foundTests.stream().filter(t -> t.getTitle().equals("Java & Spring"))
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Возвращение пустого списка для тем без тестов")
    void findTestsByTopicTitles_Criteria_shouldReturnEmptyListForTopicsWithNoTests() {
        // Arrange
        Topic topicUnused = new Topic();
        topicUnused.setTitle("Unused Topic");
        topicRepo.save(topicUnused);
        List<String> searchTitles = List.of("Unused Topic");

        // Act
        List<TestModel> foundTests = testRepoImpl.findTestsByTopicTitles(searchTitles);

        // Assert
        assertThat(foundTests).isEmpty();
    }

    @Test
    @DisplayName("Возвращение пустого списка для несуществующих названий тем")
    void findTestsByTopicTitles_Criteria_shouldReturnEmptyListForNonExistentTitles() {
        // Arrange
        List<String> searchTitles = List.of("NonExistentTopic1", "NonExistentTopic2");

        // Act
        List<TestModel> foundTests = testRepoImpl.findTestsByTopicTitles(searchTitles);

        // Assert
        assertThat(foundTests).isEmpty();
    }

    @Test
    @DisplayName("Возвращение пустого списка при передаче null или пустого списка")
    void findTestsByTopicTitles_Criteria_shouldReturnEmptyListForNullOrEmptyInput() {
        // Arrange
        List<String> emptyList = Collections.emptyList();

        // Act
        List<TestModel> resultsForEmpty = testRepoImpl.findTestsByTopicTitles(emptyList);
        List<TestModel> resultsForNull = testRepoImpl.findTestsByTopicTitles(null);

        // Assert
        assertThat(resultsForEmpty).isEmpty();
        assertThat(resultsForNull).isEmpty();
    }

    @Test
    @DisplayName("Результаты Criteria API и @Query должны совпадать по методу: findTestsByTopicTitles")
    void findTestsByTopicTitles_CriteriaAndQueryShouldMatch() {
        List<String> searchTitles = List.of("Java Basics", "Spring Framework");

        // Act
        List<TestModel> resultsFromImpl = testRepoImpl.findTestsByTopicTitles(searchTitles);
        List<TestModel> resultsFromRepo = testRepo.findTestsByTopicTitles(searchTitles);

        // Assert
        assertThat(resultsFromImpl).hasSize(3);
        assertThat(resultsFromRepo).hasSize(3);
        assertThat(resultsFromImpl).containsExactlyInAnyOrderElementsOf(resultsFromRepo);
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

    private User createAndSaveRandomUser() {
        return userRepo.save(GeneratorRandomModel.getRandomUser());
    }

    private Topic createAndSaveTopic(String topicTitle) {
        return topicRepo.save(new Topic(topicTitle));
    }

    private void linkTestTopic(TestModel test, Topic topic) {
        TestTopic link = new TestTopic();
        link.setTest(test);
        link.setTopic(topic);
        testTopicRepo.save(link);
    }
}