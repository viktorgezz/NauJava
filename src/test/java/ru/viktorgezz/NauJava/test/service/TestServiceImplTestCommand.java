package ru.viktorgezz.NauJava.test.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import ru.viktorgezz.NauJava.AbstractIntegrationPostgresTest;
import ru.viktorgezz.NauJava.question.Question;
import ru.viktorgezz.NauJava.question.QuestionRepo;
import ru.viktorgezz.NauJava.question.Type;
import ru.viktorgezz.NauJava.test.Status;
import ru.viktorgezz.NauJava.test.TestModel;
import ru.viktorgezz.NauJava.test.dto.TestRequestDto;
import ru.viktorgezz.NauJava.test.repo.TestRepo;
import ru.viktorgezz.NauJava.test.service.intrf.TestCommandService;
import ru.viktorgezz.NauJava.test_topic.TestTopic;
import ru.viktorgezz.NauJava.test_topic.TestTopicRepo;
import ru.viktorgezz.NauJava.topic.Topic;
import ru.viktorgezz.NauJava.topic.TopicRepo;
import ru.viktorgezz.NauJava.user.User;
import ru.viktorgezz.NauJava.user.repo.UserRepo;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.viktorgezz.NauJava.util.CreationModel.*;

@ActiveProfiles("test")
@DisplayName("TestServiceImpl Integration Tests")
class TestServiceImplTestCommand extends AbstractIntegrationPostgresTest {

    @Autowired
    private TestCommandService testCommandService;

    @Autowired
    private TestRepo testRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TopicRepo topicRepo;

    @Autowired
    private QuestionRepo questionRepo;

    @Autowired
    private TestTopicRepo testTopicRepo;

    private User author;
    private Topic topic1;
    private Topic topic2;

    @BeforeEach
    void setUp() {
        questionRepo.deleteAll();
        testTopicRepo.deleteAll();
        testRepo.deleteAll();
        userRepo.deleteAll();
        topicRepo.deleteAll();

        author = userRepo.save(createRandomUser());

        topic1 = topicRepo.save(createTopic("Topic One"));
        topic2 = topicRepo.save(createTopic("Topic Two"));
    }

    @Test
    @DisplayName("Создание теста с вопросами и темами")
    void shouldSuccessfullyCreateTestWithDetails() {
        // Arrange
        List<Question> questionsToCreate = List.of(
                createQuestion("Question 1 Text?", Type.SINGLE_CHOICE),
                createQuestion("Question 2 Text?", Type.OPEN_TEXT)
        );

        Set<Long> topicIdsToLink = Set.of(topic1.getId(), topic2.getId());

        TestRequestDto requestDto = new TestRequestDto(
                "Successful Test",
                "Description for success",
                Status.PUBLIC,
                author.getId(),
                questionsToCreate,
                topicIdsToLink
        );

        long initialTestCount = testRepo.count();
        long initialQuestionCount = questionRepo.count();
        long initialTestTopicCount = testTopicRepo.count();

        // Act
        TestModel createdTest = testCommandService.createTest(requestDto);

        // Assert
        assertThat(createdTest).isNotNull();
        assertThat(createdTest.getId()).isNotNull();
        assertThat(createdTest.getTitle()).isEqualTo("Successful Test");
        assertThat(createdTest.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(createdTest.getStatus()).isEqualTo(Status.PUBLIC);

        assertThat(testRepo.count()).isEqualTo(initialTestCount + 1);
        TestModel foundTest = testRepo.findById(createdTest.getId()).orElse(null);
        assertThat(foundTest).isNotNull();

        assertThat(questionRepo.count()).isEqualTo(initialQuestionCount + 2);
        List<Question> savedQuestions = questionRepo.findByTestId(createdTest.getId());
        assertThat(savedQuestions).hasSize(2);
        assertThat(savedQuestions).extracting(Question::getText).containsExactlyInAnyOrder("Question 1 Text?", "Question 2 Text?");
        assertThat(savedQuestions).allMatch(q -> q.getTest() != null && q.getTest().getId().equals(createdTest.getId()));

        assertThat(testTopicRepo.count()).isEqualTo(initialTestTopicCount + 2);
        Set<TestTopic> savedTestTopics = testTopicRepo.findByTestId(createdTest.getId());
        assertThat(savedTestTopics).hasSize(2);
        assertThat(savedTestTopics).extracting(tt -> tt.getTopic().getId()).containsExactlyInAnyOrder(topic1.getId(), topic2.getId());
        assertThat(savedTestTopics).allMatch(tt -> tt.getTest() != null && tt.getTest().getId().equals(createdTest.getId()));
    }

    @Test
    @DisplayName("Откат транзакции при несуществующем ID автора")
    void shouldRollbackTransactionWhenAuthorNotFound() {
        // Arrange
        Long nonExistentAuthorId = 999L;
        List<Question> questionsToCreate = List.of(new Question());
        Set<Long> topicIdsToLink = Set.of(topic1.getId());

        TestRequestDto requestDto = new TestRequestDto(
                "Failed Test Author",
                "Description",
                Status.PRIVATE,
                nonExistentAuthorId,
                questionsToCreate,
                topicIdsToLink
        );

        long initialTestCount = testRepo.count();
        long initialQuestionCount = questionRepo.count();
        long initialTestTopicCount = testTopicRepo.count();

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> testCommandService.createTest(requestDto)
        );

        assertThat(exception.getMessage()).isEqualTo("Author not found with id: " + nonExistentAuthorId);

        // Assert
        assertThat(testRepo.count()).isEqualTo(initialTestCount);
        assertThat(questionRepo.count()).isEqualTo(initialQuestionCount);
        assertThat(testTopicRepo.count()).isEqualTo(initialTestTopicCount);
    }

    @Test
    @DisplayName("Откат транзакции при несуществующем ID темы")
    void shouldRollbackTransactionWhenTopicNotFound() {
        // Arrange
        Long nonExistentTopicId = 998L;
        List<Question> questionsToCreate = List.of(new Question());
        Set<Long> topicIdsToLink = Set.of(topic1.getId(), nonExistentTopicId);

        TestRequestDto requestDto = new TestRequestDto(
                "Failed Test Topic",
                "Description",
                Status.UNLISTED,
                author.getId(),
                questionsToCreate,
                topicIdsToLink
        );

        long initialTestCount = testRepo.count();
        long initialQuestionCount = questionRepo.count();
        long initialTestTopicCount = testTopicRepo.count();

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> testCommandService.createTest(requestDto)
        );

        assertThat(exception.getMessage()).isEqualTo("One or more topics not found for the given IDs.");

        // Assert
        assertThat(testRepo.count()).isEqualTo(initialTestCount);
        assertThat(questionRepo.count()).isEqualTo(initialQuestionCount);
        assertThat(testTopicRepo.count()).isEqualTo(initialTestTopicCount);
    }

    @Test
    @DisplayName("Успешно создание теста без вопросов и тем")
    void shouldCreateTestWithoutQuestionsAndTopics() {
        // Arrange
        List<Question> emptyQuestions = Collections.emptyList();
        Set<Long> emptyTopicIds = Collections.emptySet();

        TestRequestDto requestDto = new TestRequestDto(
                "Simple Test",
                "Simple Desc",
                Status.PRIVATE,
                author.getId(),
                emptyQuestions,
                emptyTopicIds
        );

        long initialTestCount = testRepo.count();
        long initialQuestionCount = questionRepo.count();
        long initialTestTopicCount = testTopicRepo.count();

        TestModel createdTest = testCommandService.createTest(requestDto);

        // Assert
        assertThat(createdTest).isNotNull();
        assertThat(createdTest.getId()).isNotNull();
        assertThat(createdTest.getTitle()).isEqualTo("Simple Test");
        assertThat(createdTest.getAuthor().getId()).isEqualTo(author.getId());

        assertThat(testRepo.count()).isEqualTo(initialTestCount + 1);
        assertThat(questionRepo.count()).isEqualTo(initialQuestionCount);
        assertThat(testTopicRepo.count()).isEqualTo(initialTestTopicCount);
    }
}