package ru.viktorgezz.testing_system.domain.test.repo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.viktorgezz.testing_system.domain.many_to_many_entity.test_topic.TestTopicRepo;
import ru.viktorgezz.testing_system.domain.test.Status;
import ru.viktorgezz.testing_system.domain.test.TestModel;
import ru.viktorgezz.testing_system.domain.topic.repo.TopicRepo;
import ru.viktorgezz.testing_system.domain.user.repo.UserRepo;
import ru.viktorgezz.testing_system.testconfig.AbstractIntegrationPostgresTest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.viktorgezz.testing_system.util.CreationModel.createTest;
import static ru.viktorgezz.testing_system.util.CreationModel.createUserRandom;

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

    private Long idAuthor;

    @BeforeEach
    void setUp() {
        idAuthor = userRepo.save(createUserRandom()).getId();
    }

    @AfterEach
    void tearDown() {
        testTopicRepo.deleteAll();
        testRepo.deleteAll();
        topicRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("findAllWithAuthorAndTopicsByIds: возврат тестов по списку id")
    void findAllWithAuthorAndTopicsByIds_ShouldReturnTestsForSpecifiedIds_WhenTestsExist() {
        TestModel testFirst = testRepo.save(createTest("Title A", "Desc A", Status.PUBLIC, userRepo.findById(idAuthor).orElseThrow()));
        TestModel testSecond = testRepo.save(createTest("Title B", "Desc B", Status.PUBLIC, userRepo.findById(idAuthor).orElseThrow()));
        testRepo.save(createTest("Title C", "Desc C", Status.PUBLIC, userRepo.findById(idAuthor).orElseThrow()));

        List<TestModel> testsFound = testRepo.findAllWithAuthorAndTopicsByIds(List.of(testFirst.getId(), testSecond.getId()));

        assertThat(testsFound).hasSize(2);
        Set<Long> idsFound = testsFound.stream()
                .map(TestModel::getId)
                .collect(java.util.stream.Collectors.toSet());
        assertThat(idsFound).containsExactlyInAnyOrder(testFirst.getId(), testSecond.getId());
    }

    @Test
    @DisplayName("findForEditingContent: возврат теста с вопросами и автором по id")
    void findForEditingContent_ShouldReturnTestWithQuestionsAndAuthor_WhenTestExists() {
        TestModel testSaved = testRepo.save(createTest("Title A", "Desc A", Status.PUBLIC, userRepo.findById(idAuthor).orElseThrow()));

        Optional<TestModel> testFoundOptional = testRepo.findForEditingContent(testSaved.getId());

        assertThat(testFoundOptional).isPresent();
        TestModel testFound = testFoundOptional.get();
        assertThat(testFound.getAuthor()).isNotNull();
        assertThat(testFound.getAuthor().getId()).isEqualTo(idAuthor);
        assertThat(testFound.getQuestions()).isNotNull();
    }

    @Test
    @DisplayName("findForEditingMetadata: возврат теста с автором и темами по id")
    void findForEditingMetadata_ShouldReturnTestWithAuthorAndTopics_WhenTestExists() {
        TestModel testSaved = testRepo.save(createTest("Title A", "Desc A", Status.PUBLIC, userRepo.findById(idAuthor).orElseThrow()));

        Optional<TestModel> testFoundOptional = testRepo.findForEditingMetadata(testSaved.getId());

        assertThat(testFoundOptional).isPresent();
        TestModel testFound = testFoundOptional.get();
        assertThat(testFound.getAuthor()).isNotNull();
        assertThat(testFound.getAuthor().getId()).isEqualTo(idAuthor);
        assertThat(testFound.getTestTopics()).isNotNull();
    }
}