package ru.viktorgezz.NauJava.domain.test.service;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.domain.answer_option.repo.AnswerOptionRepo;
import ru.viktorgezz.NauJava.domain.answer_option.service.intrf.AnswerOptionQueryService;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.question.repo.QuestionRepo;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.dto.TestToPassDto;
import ru.viktorgezz.NauJava.domain.test.dto.TestUpdateContentDto;
import ru.viktorgezz.NauJava.domain.test.repo.TestRepo;
import ru.viktorgezz.NauJava.domain.test.service.intrf.TestQueryService;
import ru.viktorgezz.NauJava.domain.topic.Topic;
import ru.viktorgezz.NauJava.domain.topic.repo.TopicRepo;
import ru.viktorgezz.NauJava.domain.topic.service.TopicService;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationPostgresTest;
import ru.viktorgezz.NauJava.util.CreationModel;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
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
    @Autowired
    private QuestionRepo questionRepo;
    @Autowired
    private AnswerOptionRepo answerOptionRepo;
    @MockitoBean
    private AnswerOptionQueryService answerOptionQueryService;

    private TestModel testModelFirst;
    private TestModel testModelSecond;
    private User authorFirst;
    private Topic topicFirst;
    private Topic topicSecond;
    private Topic topicCommon;

    @BeforeEach
    void setup() {
        authorFirst = CreationModel.createUserRandom();
        userRepo.save(authorFirst);

        testModelFirst = createTestRadom(authorFirst);
        testModelSecond = createTestRadom(authorFirst);

        testRepo.saveAll(List.of(testModelFirst, testModelSecond));

        topicFirst = createTopicRandom();
        topicSecond = createTopicRandom();
        topicCommon = createTopicRandom();

        topicRepo.saveAll(List.of(topicFirst, topicSecond, topicCommon));

        topicService.saveAndLinkAll(Set.of(topicFirst, topicCommon), testModelFirst);
        topicService.saveAndLinkAll(Set.of(topicSecond, topicCommon), testModelSecond);
    }

    @AfterEach
    void tearDown() {
        answerOptionRepo.deleteAll();
        questionRepo.deleteAll();
        userRepo.deleteAll();
        testRepo.deleteAll();
        topicRepo.deleteAll();
    }

    @Test
    @DisplayName("findAllWithAuthorAndTopics: возвращает все тесты с авторами и темами")
    void findAllWithAuthorAndTopics_ShouldReturnAllTestsWithRelations() {
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<TestModel> pageActual = testQueryService.findAllWithAuthorAndTopics(pageRequest);

        assertThat(pageActual.getTotalElements()).isEqualTo(2);
        assertThat(pageActual.getContent()).hasSize(2);
        assertThat(pageActual.getContent())
                .extracting(TestModel::getId)
                .containsExactly(testModelFirst.getId(), testModelSecond.getId());

        TestModel firstTest = pageActual.getContent().getFirst();
        assertThat(firstTest.getAuthor()).isNotNull();
        assertThat(firstTest.getAuthor()).isEqualTo(authorFirst);
        assertThat(firstTest.getTopics())
                .extracting(Topic::getTitle)
                .containsExactlyInAnyOrder(topicFirst.getTitle(), topicCommon.getTitle());

        TestModel secondTest = pageActual.getContent().get(1);
        assertThat(secondTest.getAuthor()).isNotNull();
        assertThat(secondTest.getAuthor()).isEqualTo(authorFirst);
        assertThat(secondTest.getTopics())
                .extracting(Topic::getTitle)
                .containsExactlyInAnyOrder(topicSecond.getTitle(), topicCommon.getTitle());
    }

    @Test
    @DisplayName("findAllWithAuthorAndTopics: возвращает пустую страницу когда тестов нет")
    void findAllWithAuthorAndTopics_ShouldReturnEmptyPage_WhenNoTests() {
        testRepo.deleteAll();
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<TestModel> pageActual = testQueryService.findAllWithAuthorAndTopics(pageRequest);

        assertThat(pageActual.getTotalElements()).isEqualTo(0);
        assertThat(pageActual.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findAllWithAuthorAndTopics: корректно работает с пагинацией")
    void findAllWithAuthorAndTopics_ShouldReturnPaginatedResults() {
        User authorAdditional = CreationModel.createUserRandom();
        userRepo.save(authorAdditional);
        TestModel testModelThird = createTestRadom(authorAdditional);
        testRepo.save(testModelThird);

        PageRequest pageRequestFirst = PageRequest.of(0, 2);
        PageRequest pageRequestSecond = PageRequest.of(1, 2);

        Page<TestModel> pageFirst = testQueryService.findAllWithAuthorAndTopics(pageRequestFirst);
        Page<TestModel> pageSecond = testQueryService.findAllWithAuthorAndTopics(pageRequestSecond);

        assertThat(pageFirst.getTotalElements()).isEqualTo(3);
        assertThat(pageFirst.getContent()).hasSize(2);
        assertThat(pageFirst.getTotalPages()).isEqualTo(2);

        assertThat(pageSecond.getTotalElements()).isEqualTo(3);
        assertThat(pageSecond.getContent()).hasSize(1);
        assertThat(pageSecond.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByTitle: возвращает тесты по частичному совпадению заголовка")
    void findByTitle_ShouldReturnTests_WhenPartialMatch() {
        String titleFirst = testModelFirst.getTitle();
        String searchSubstring = titleFirst.substring(0, Math.min(5, titleFirst.length()));
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<TestModel> pageActual = testQueryService.findByTitle(searchSubstring, pageRequest);

        assertThat(pageActual.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(pageActual.getContent())
                .extracting(TestModel::getTitle)
                .contains(testModelFirst.getTitle());
    }

    @Test
    @DisplayName("findByTitle: возвращает пустую страницу когда совпадений нет")
    void findByTitle_ShouldReturnEmptyPage_WhenNoMatches() {
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<TestModel> pageActual = testQueryService.findByTitle("NonExistentTitle12345", pageRequest);

        assertThat(pageActual.getTotalElements()).isEqualTo(0);
        assertThat(pageActual.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findByTitle: нечувствителен к регистру")
    void findByTitle_ShouldBeCaseInsensitive() {
        String titleFirst = testModelFirst.getTitle();
        String titleLower = titleFirst.toLowerCase();
        String titleUpper = titleFirst.toUpperCase();
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<TestModel> pageLower = testQueryService.findByTitle(titleLower, pageRequest);
        Page<TestModel> pageUpper = testQueryService.findByTitle(titleUpper, pageRequest);

        assertThat(pageLower.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(pageUpper.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(pageLower.getContent())
                .extracting(TestModel::getTitle)
                .contains(testModelFirst.getTitle());
        assertThat(pageUpper.getContent())
                .extracting(TestModel::getTitle)
                .contains(testModelFirst.getTitle());
    }

    @Test
    @DisplayName("findById: возвращает тест при существующем id")
    void findById_ShouldReturnTest_WhenIdExists() {
        TestModel testSaved = testRepo.save(createTest("Title Single", "Desc", Status.PUBLIC, authorFirst));

        TestModel testFound = testQueryService.findById(testSaved.getId());

        assertThat(testFound.getId()).isEqualTo(testSaved.getId());
        assertThat(testFound.getAuthor()).isEqualTo(authorFirst);
    }

    @Test
    @DisplayName("findById: выбрасывает EntityNotFoundException при отсутствии теста")
    void findById_ShouldThrowEntityNotFoundException_WhenIdDoesNotExist() {
        Long idNonExistent = 999999L;

        assertThatThrownBy(() -> testQueryService.findById(idNonExistent))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    @DisplayName("findTestToPassById: возвращает DTO теста с вопросами и вариантами ответов")
    void findTestToPassById_ShouldReturnDtoWithQuestionsAndAnswerOptions_WhenTestExists() {
        TestModel testSaved = testRepo.save(createTest("Test To Pass", "Desc", Status.PUBLIC, authorFirst));
        Question questionSaved = questionRepo.save(createQuestionSingleChoice("Question text", java.math.BigDecimal.ONE, testSaved));
        AnswerOption answerOptionCorrect = answerOptionRepo.save(createAnswerOption("Answer", true, questionSaved));
        AnswerOption answerOptionWrong = answerOptionRepo.save(createAnswerOption("Answer wrong", false, questionSaved));

        when(answerOptionQueryService.findAllAnswerOptionByIdsQuestionWithQuestion(anyList()))
                .thenReturn(List.of(answerOptionCorrect, answerOptionWrong));

        TestToPassDto dto = testQueryService.findTestToPassById(testSaved.getId());

        assertThat(dto.idTest()).isEqualTo(testSaved.getId());
        assertThat(dto.questionsDto()).hasSize(1);
        TestToPassDto.QuestionDto questionDto = dto.questionsDto().getFirst();
        assertThat(questionDto.idQuestion()).isEqualTo(questionSaved.getId());
        assertThat(questionDto.answerOptions()).hasSize(2);
        verify(answerOptionQueryService, times(2)).findAllAnswerOptionByIdsQuestionWithQuestion(List.of(questionSaved.getId()));
    }

    @Test
    @DisplayName("findByIdWithContent: возвращает DTO теста для редактирования содержимого")
    void findByIdWithContent_ShouldReturnDtoWithQuestionsAndAnswerOptions_WhenTestExists() {
        TestModel testSaved = testRepo.save(createTest("Test Content", "Desc", Status.PUBLIC, authorFirst));
        Question questionSaved = questionRepo.save(createQuestionSingleChoice("Question text", java.math.BigDecimal.TEN, testSaved));
        AnswerOption answerOptionCorrect = answerOptionRepo.save(createAnswerOption("Answer", true, questionSaved));
        AnswerOption answerOptionWrong = answerOptionRepo.save(createAnswerOption("Answer wrong", false, questionSaved));
        when(answerOptionQueryService.findAllAnswerOptionByIdsQuestionWithQuestion(anyList()))
                .thenReturn(List.of(answerOptionCorrect, answerOptionWrong));

        TestUpdateContentDto dto = testQueryService.findByIdWithContent(testSaved.getId());

        assertThat(dto.idTest()).isEqualTo(testSaved.getId());
        assertThat(dto.questions()).hasSize(1);
        TestUpdateContentDto.QuestionDto questionDto = dto.questions().getFirst();
        assertThat(questionDto.idQuestion()).isEqualTo(questionSaved.getId());
        assertThat(questionDto.answerOptions()).hasSize(2);
        verify(answerOptionQueryService).findAllAnswerOptionByIdsQuestionWithQuestion(List.of(questionSaved.getId()));
    }
}
