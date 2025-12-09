package ru.viktorgezz.NauJava.domain.answer_option.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.domain.answer_option.repo.AnswerOptionRepo;
import ru.viktorgezz.NauJava.domain.answer_option.service.intrf.AnswerOptionQueryService;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.question.repo.QuestionRepo;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.repo.TestRepo;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationPostgresTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.viktorgezz.NauJava.util.CreationModel.*;

@DisplayName("AnswerOptionQueryService Integration Tests")
class AnswerOptionQueryServiceTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private AnswerOptionQueryService answerOptionQueryService;

    @Autowired
    private AnswerOptionRepo answerOptionRepo;

    @Autowired
    private QuestionRepo questionRepo;

    @Autowired
    private TestRepo testRepo;

    @Autowired
    private UserRepo userRepo;

    private Question questionFirst;
    private Question questionSecond;
    private AnswerOption answerOptionFirstQuestionFirst;
    private AnswerOption answerOptionSecondQuestionFirst;

    @BeforeEach
    void setUp() {
        User authorTest = userRepo.save(createUserRandom());

        TestModel testModelFirst = createTest("Test Title", "Test Description", Status.PUBLIC, authorTest);
        testRepo.save(testModelFirst);

        questionFirst = createQuestionSingleChoice("Question First?", new BigDecimal("10.00"), testModelFirst);
        questionSecond = createQuestionSingleChoice("Question Second?", new BigDecimal("5.00"), testModelFirst);
        Question questionThird = createQuestionSingleChoice("Question Third?", new BigDecimal("15.00"), testModelFirst);
        questionRepo.saveAll(List.of(questionFirst, questionSecond, questionThird));

        answerOptionFirstQuestionFirst = createAnswerOption("Answer First Q1", true, questionFirst);
        answerOptionSecondQuestionFirst = createAnswerOption("Answer Second Q1", false, questionFirst);
        AnswerOption answerOptionFirstQuestionSecond = createAnswerOption("Answer First Q2", true, questionSecond);
        AnswerOption answerOptionFirstQuestionThird = createAnswerOption("Answer First Q3", false, questionThird);
        answerOptionRepo.saveAll(List.of(
                answerOptionFirstQuestionFirst,
                answerOptionSecondQuestionFirst,
                answerOptionFirstQuestionSecond,
                answerOptionFirstQuestionThird
        ));
    }

    @AfterEach
    void tearDown() {
        answerOptionRepo.deleteAll();
        questionRepo.deleteAll();
        testRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("Успешное получение вариантов ответов для существующих вопросов с подгруженными вопросами")
    void findAllAnswerOptionByIdsQuestionWithQuestion_ShouldReturnAnswerOptions_WhenQuestionsExist() {
        List<Long> idsQuestion = List.of(questionFirst.getId(), questionSecond.getId());

        List<AnswerOption> answerOptionsFound = answerOptionQueryService.findAllAnswerOptionByIdsQuestionWithQuestion(idsQuestion);

        assertThat(answerOptionsFound).hasSize(3);
        Set<Long> idsQuestionFound = answerOptionsFound.stream()
                .map(answerOption -> answerOption.getQuestion().getId())
                .collect(Collectors.toSet());
        assertThat(idsQuestionFound).containsExactlyInAnyOrder(questionFirst.getId(), questionSecond.getId());
        answerOptionsFound.forEach(answerOption -> {
            assertThat(answerOption.getQuestion()).isNotNull();
            assertThat(answerOption.getQuestion().getId()).isNotNull();
        });
    }

    @Test
    @DisplayName("Возврат пустого списка для несуществующих вопросов")
    void findAllAnswerOptionByIdsQuestionWithQuestion_ShouldReturnEmptyList_WhenQuestionsDoNotExist() {
        List<Long> idsQuestionNonExistent = List.of(9999L, 10000L);

        List<AnswerOption> answerOptionsFound = answerOptionQueryService.findAllAnswerOptionByIdsQuestionWithQuestion(idsQuestionNonExistent);

        assertThat(answerOptionsFound).isEmpty();
    }

    @Test
    @DisplayName("Возврат только вариантов ответов для указанных вопросов при наличии других вопросов")
    void findAllAnswerOptionByIdsQuestionWithQuestion_ShouldReturnOnlySpecifiedQuestionsOptions_WhenMultipleQuestionsExist() {
        List<Long> idsQuestionSpecified = List.of(questionFirst.getId());

        List<AnswerOption> answerOptionsFound = answerOptionQueryService.findAllAnswerOptionByIdsQuestionWithQuestion(idsQuestionSpecified);

        assertThat(answerOptionsFound).hasSize(2);
        answerOptionsFound.forEach(answerOption ->
                assertThat(answerOption.getQuestion().getId()).isEqualTo(questionFirst.getId())
        );
        Set<Long> idsAnswerOptionFound = answerOptionsFound.stream()
                .map(AnswerOption::getId)
                .collect(Collectors.toSet());
        assertThat(idsAnswerOptionFound).containsExactlyInAnyOrder(
                answerOptionFirstQuestionFirst.getId(),
                answerOptionSecondQuestionFirst.getId()
        );
    }
}
