package ru.viktorgezz.NauJava.domain.result.repo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.question.repo.QuestionRepo;
import ru.viktorgezz.NauJava.domain.result.Grade;
import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.repo.TestRepo;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationPostgresTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.viktorgezz.NauJava.util.CreationModel.*;

@DisplayName("ResultRepo Integration Tests")
class ResultRepoTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private ResultRepo resultRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TestRepo testRepo;

    @Autowired
    private QuestionRepo questionRepo;

    private User userParticipantFirst;
    private User userParticipantSecond;
    private TestModel testModelFirst;
    private TestModel testModelSecond;
    private Question questionFirstTestFirst;
    private Question questionSecondTestFirst;
    private Question questionThirdTestFirst;

    @BeforeEach
    void setUp() {
        userParticipantFirst = userRepo.save(createUserRandom());
        userParticipantSecond = userRepo.save(createUserRandom());
        User userAuthorTest = userRepo.save(createUserRandom());

        testModelFirst = createTest("Test Title First", "Test Description First", Status.PUBLIC, userAuthorTest);
        testModelSecond = createTest("Test Title Second", "Test Description Second", Status.PUBLIC, userAuthorTest);
        testRepo.saveAll(List.of(testModelFirst, testModelSecond));

        questionFirstTestFirst = createQuestionSingleChoice("Question First?", new BigDecimal("10.00"), testModelFirst);
        questionSecondTestFirst = createQuestionSingleChoice("Question Second?", new BigDecimal("5.00"), testModelFirst);
        questionThirdTestFirst = createQuestionSingleChoice("Question Third?", new BigDecimal("15.00"), testModelFirst);
        questionRepo.saveAll(List.of(questionFirstTestFirst, questionSecondTestFirst, questionThirdTestFirst));
    }

    @AfterEach
    void tearDown() {
        resultRepo.deleteAll();
        questionRepo.deleteAll();
        testRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("findAll: возврат всех результатов когда они существуют")
    void findAll_ShouldReturnAllResults_WhenResultsExist() {
        Result resultFirst = createResultWithTest(userParticipantFirst, testModelFirst, Grade.A, new BigDecimal("95.00"), 120);
        Result resultSecond = createResultWithTest(userParticipantSecond, testModelSecond, Grade.B, new BigDecimal("80.00"), 180);
        resultRepo.saveAll(List.of(resultFirst, resultSecond));

        List<Result> resultsFound = resultRepo.findAll();

        assertThat(resultsFound).hasSize(2);
        Set<Long> idsResultFound = resultsFound.stream()
                .map(Result::getId)
                .collect(Collectors.toSet());
        assertThat(idsResultFound).containsExactlyInAnyOrder(resultFirst.getId(), resultSecond.getId());
    }

    @Test
    @DisplayName("findAll: возврат пустого списка когда результатов нет")
    void findAll_ShouldReturnEmptyList_WhenNoResultsExist() {
        List<Result> resultsFound = resultRepo.findAll();

        assertThat(resultsFound).isEmpty();
    }

    @Test
    @DisplayName("findAllWithParticipantUsernameAndTitleTest: возврат результатов с загруженными participant и test")
    void findAllWithParticipantUsernameAndTitleTest_ShouldReturnResultsWithLoadedParticipantAndTest_WhenResultsExist() {
        Result resultFirst = createResultWithTest(userParticipantFirst, testModelFirst, Grade.A, new BigDecimal("95.00"), 120);
        Result resultSecond = createResultWithTest(userParticipantSecond, testModelSecond, Grade.B, new BigDecimal("80.00"), 180);
        resultRepo.saveAll(List.of(resultFirst, resultSecond));

        List<Result> resultsFound = resultRepo.findAllWithParticipantUsernameAndTitleTest();

        assertThat(resultsFound).hasSize(2);
        resultsFound.forEach(resultFound -> {
            assertThat(resultFound.getParticipant()).isNotNull();
            assertThat(resultFound.getParticipant().getUsername()).isNotNull();
            assertThat(resultFound.getTest()).isNotNull();
            assertThat(resultFound.getTest().getTitle()).isNotNull();
        });
        Set<Long> idsResultFound = resultsFound.stream()
                .map(Result::getId)
                .collect(Collectors.toSet());
        assertThat(idsResultFound).containsExactlyInAnyOrder(resultFirst.getId(), resultSecond.getId());
    }

    @Test
    @DisplayName("findAllWithParticipantUsernameAndTitleTest: возврат пустого списка когда результатов нет")
    void findAllWithParticipantUsernameAndTitleTest_ShouldReturnEmptyList_WhenNoResultsExist() {
        List<Result> resultsFound = resultRepo.findAllWithParticipantUsernameAndTitleTest();

        assertThat(resultsFound).isEmpty();
    }

    @Test
    @DisplayName("findByIdWithTestAndQuestions: возврат результата с загруженными test и questions когда результат существует")
    void findByIdWithTestAndQuestions_ShouldReturnResultWithLoadedTestAndQuestions_WhenResultExists() {
        Result resultFirst = createResultWithTest(userParticipantFirst, testModelFirst, Grade.A, new BigDecimal("95.00"), 120);
        resultRepo.save(resultFirst);

        Optional<Result> resultFoundOptional = resultRepo.findByIdWithTestAndQuestions(resultFirst.getId());

        assertThat(resultFoundOptional).isPresent();
        Result resultFound = resultFoundOptional.get();
        assertThat(resultFound.getTest()).isNotNull();
        assertThat(resultFound.getTest().getTitle()).isEqualTo(testModelFirst.getTitle());
        assertThat(resultFound.getTest().getQuestions()).isNotNull();
        assertThat(resultFound.getTest().getQuestions()).hasSize(3);
        Set<Long> idsQuestionFound = resultFound.getTest().getQuestions().stream()
                .map(Question::getId)
                .collect(Collectors.toSet());
        assertThat(idsQuestionFound).containsExactlyInAnyOrder(
                questionFirstTestFirst.getId(),
                questionSecondTestFirst.getId(),
                questionThirdTestFirst.getId()
        );
    }

    @Test
    @DisplayName("findByIdWithTestAndQuestions: возврат пустого Optional когда результат не существует")
    void findByIdWithTestAndQuestions_ShouldReturnEmptyOptional_WhenResultDoesNotExist() {
        Long idResultNonExistent = 999L;

        Optional<Result> resultFoundOptional = resultRepo.findByIdWithTestAndQuestions(idResultNonExistent);

        assertThat(resultFoundOptional).isEmpty();
    }

    @Test
    @DisplayName("findByIdWithTestAndQuestions: возврат результата с несколькими вопросами")
    void findByIdWithTestAndQuestions_ShouldReturnResultWithMultipleQuestions_WhenTestHasMultipleQuestions() {
        Result resultFirst = createResultWithTest(userParticipantFirst, testModelFirst, Grade.A, new BigDecimal("95.00"), 120);
        resultRepo.save(resultFirst);

        Optional<Result> resultFoundOptional = resultRepo.findByIdWithTestAndQuestions(resultFirst.getId());

        assertThat(resultFoundOptional).isPresent();
        Result resultFound = resultFoundOptional.get();
        assertThat(resultFound.getTest().getQuestions()).hasSize(3);
        List<String> textsQuestionFound = resultFound.getTest().getQuestions().stream()
                .map(Question::getText)
                .collect(Collectors.toList());
        assertThat(textsQuestionFound).containsExactlyInAnyOrder(
                questionFirstTestFirst.getText(),
                questionSecondTestFirst.getText(),
                questionThirdTestFirst.getText()
        );
    }

    @Test
    @DisplayName("findAllWithTestByIds: возврат результатов по списку ID с загруженным тестом")
    void findAllWithTestByIds_ShouldReturnResultsWithTest_WhenIdsExist() {
        Result resultFirst = createResultWithTest(userParticipantFirst, testModelFirst, Grade.A, new BigDecimal("95.00"), 120);
        Result resultSecond = createResultWithTest(userParticipantSecond, testModelSecond, Grade.B, new BigDecimal("80.00"), 180);
        resultRepo.saveAll(List.of(resultFirst, resultSecond));

        List<Result> resultsFound = resultRepo.findAllWithTestByIds(List.of(resultFirst.getId(), resultSecond.getId()));

        assertThat(resultsFound).hasSize(2);
        resultsFound.forEach(resultFound -> {
            assertThat(resultFound.getTest()).isNotNull();
            assertThat(resultFound.getTest().getTitle()).isNotNull();
        });
        Set<Long> idsResultFound = resultsFound.stream()
                .map(Result::getId)
                .collect(Collectors.toSet());
        assertThat(idsResultFound).containsExactlyInAnyOrder(resultFirst.getId(), resultSecond.getId());
    }

    @Test
    @DisplayName("findAllWithTestByIds: возврат пустого списка когда ID не существуют")
    void findAllWithTestByIds_ShouldReturnEmptyList_WhenIdsDoNotExist() {
        List<Result> resultsFound = resultRepo.findAllWithTestByIds(List.of(999L, 888L));

        assertThat(resultsFound).isEmpty();
    }

    @Test
    @DisplayName("findResultLastAttempts: возврат результатов отсортированных по дате завершения")
    @Transactional
    void findResultLastAttempts_ShouldReturnResultsSortedByCompletedAtDesc_WhenResultsExist() {
        LocalDateTime baseTime = LocalDateTime.now();
        Result resultFirst = createResultWithTest(userParticipantFirst, testModelFirst, Grade.A, new BigDecimal("95.00"), 120);
        resultFirst.setCompletedAt(baseTime.minusDays(1));
        Result resultSecond = createResultWithTest(userParticipantFirst, testModelFirst, Grade.B, new BigDecimal("80.00"), 180);
        resultSecond.setCompletedAt(baseTime);
        resultRepo.saveAll(List.of(resultFirst, resultSecond));

        List<Result> resultsFound = resultRepo.findResultLastAttempts(testModelFirst.getId(), userParticipantFirst.getId())
                .toList();

        assertThat(resultsFound).hasSize(2);
        assertThat(resultsFound.getFirst().getId()).isEqualTo(resultSecond.getId());
        assertThat(resultsFound.get(1).getId()).isEqualTo(resultFirst.getId());
    }

    @Test
    @DisplayName("findResultLastAttempts: возврат только результатов указанного пользователя и теста")
    @Transactional
    void findResultLastAttempts_ShouldReturnOnlySpecifiedUserAndTestResults_WhenMultipleUsersHaveResults() {
        Result resultFirstUser = createResultWithTest(userParticipantFirst, testModelFirst, Grade.A, new BigDecimal("95.00"), 120);
        Result resultSecondUser = createResultWithTest(userParticipantSecond, testModelFirst, Grade.B, new BigDecimal("80.00"), 180);
        resultRepo.saveAll(List.of(resultFirstUser, resultSecondUser));

        List<Result> resultsFound = resultRepo.findResultLastAttempts(testModelFirst.getId(), userParticipantFirst.getId())
                .toList();

        assertThat(resultsFound).hasSize(1);
        assertThat(resultsFound.getFirst().getId()).isEqualTo(resultFirstUser.getId());
        assertThat(resultsFound.getFirst().getParticipant().getId()).isEqualTo(userParticipantFirst.getId());
    }
}