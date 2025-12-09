package ru.viktorgezz.NauJava.domain.test.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.domain.answer_option.repo.AnswerOptionRepo;
import ru.viktorgezz.NauJava.domain.many_to_many_entity.test_topic.TestTopicRepo;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.question.Type;
import ru.viktorgezz.NauJava.domain.question.repo.QuestionRepo;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.dto.TestMetadataRequestDto;
import ru.viktorgezz.NauJava.domain.test.dto.TestUpdateTestContentDto;
import ru.viktorgezz.NauJava.domain.test.repo.TestRepo;
import ru.viktorgezz.NauJava.domain.test.service.intrf.TestCommandService;
import ru.viktorgezz.NauJava.domain.topic.Topic;
import ru.viktorgezz.NauJava.domain.topic.repo.TopicRepo;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationPostgresTest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.viktorgezz.NauJava.util.CreationModel.*;
import static ru.viktorgezz.NauJava.util.GeneratorRandom.getRandomString;

@DisplayName("TestService Integration Tests")
class TestCommandServiceTest extends AbstractIntegrationPostgresTest {

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

    @Autowired
    private AnswerOptionRepo answerOptionRepo;

    private User author;
    private Topic topic1;
    private Topic topic2;

    @BeforeEach
    void setUp() {
        author = userRepo.save(createUserRandom());

        topic1 = topicRepo.save(createTopic("topic one"));
        topic2 = topicRepo.save(createTopic("topic two"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        answerOptionRepo.deleteAll();
        questionRepo.deleteAll();
        testTopicRepo.deleteAll();
        testRepo.deleteAll();
        topicRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    @DisplayName("Создание метаданных теста с темами")
    void updateTestMetadata_ShouldCreateTestWithTopics_WhenMetadataIsNew() {
        setSecurityContext(author);

        List<String> topicsTitles = List.of(topic1.getTitle(), "Topic Extra");
        TestMetadataRequestDto testMetaDto = new TestMetadataRequestDto(
                null,
                getRandomString(6),
                getRandomString(5),
                Status.PUBLIC,
                topicsTitles
        );

        long initialTestCount = testRepo.count();

        Long idTestSaved = testCommandService.updateTestMetadata(testMetaDto);

        TestModel testSaved = testRepo.findForEditingMetadata(idTestSaved).orElseThrow();
        Set<String> titlesSaved = testSaved.getTopics().stream()
                .map(Topic::getTitle)
                .collect(Collectors.toSet());
        Set<String> titlesExpected = topicsTitles.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        assertThat(testRepo.count()).isEqualTo(initialTestCount + 1); // количество тестов увеличилось на 1
        assertThat(testSaved.getTitle()).isEqualTo(testMetaDto.title());
        assertThat(testSaved.getDescription()).isEqualTo(testMetaDto.description());
        assertThat(testSaved.getStatus()).isEqualTo(testMetaDto.status());
        assertThat(testSaved.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(titlesSaved).containsExactlyInAnyOrderElementsOf(titlesExpected);
    }

    @Test
    @DisplayName("Обновление метаданных теста и замена тем")
    void updateTestMetadata_ShouldUpdateExistingTestAndReplaceTopics_WhenMetadataExists() {
        TestModel testExisting = createTest(getRandomString(6), getRandomString(5), Status.PRIVATE, author);
        testExisting.addTopic(topic1);
        testExisting.addTopic(topic2);
        testExisting = testRepo.save(testExisting);

        setSecurityContext(author);

        List<String> topicsTitlesUpdated = List.of(topic1.getTitle(), "Topic Fresh");
        TestMetadataRequestDto dtoMetadataUpdate = new TestMetadataRequestDto(
                testExisting.getId(),
                getRandomString(7),
                getRandomString(3),
                Status.PUBLIC,
                topicsTitlesUpdated
        );

        Long idTestUpdated = testCommandService.updateTestMetadata(dtoMetadataUpdate);

        TestModel testUpdated = testRepo.findForEditingMetadata(idTestUpdated).orElseThrow();
        Set<String> titlesUpdatedSaved = testUpdated.getTopics().stream()
                .map(Topic::getTitle)
                .collect(Collectors.toSet());
        Set<String> titlesUpdatedExpected = topicsTitlesUpdated.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        assertThat(testUpdated.getId()).isEqualTo(testExisting.getId());
        assertThat(testUpdated.getTitle()).isEqualTo(dtoMetadataUpdate.title());
        assertThat(testUpdated.getDescription()).isEqualTo(dtoMetadataUpdate.description());
        assertThat(testUpdated.getStatus()).isEqualTo(dtoMetadataUpdate.status());
        assertThat(testUpdated.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(titlesUpdatedSaved).containsExactlyInAnyOrderElementsOf(titlesUpdatedExpected);
        assertThat(titlesUpdatedSaved).doesNotContain(topic2.getTitle());
    }

    @Test
    @DisplayName("Создание вопросов при добавлении контента в пустой тест")
    void updateTestContent_ShouldCreateQuestions_WhenTestIsEmpty() {
        UpdateContentResult resultUpdateContent = updateEmptyTestAndCollectResult();

        assertThat(questionRepo.count()).isEqualTo(resultUpdateContent.initialQuestionCount() + 2);
        assertThat(resultUpdateContent.questionsSaved()).hasSize(2);
        assertThat(resultUpdateContent.questionSingleSaved().getText()).isEqualTo(resultUpdateContent.data().questionDtoSingle().text());
        assertThat(resultUpdateContent.questionSingleSaved().getPoint()).isEqualByComparingTo(resultUpdateContent.data().questionDtoSingle().point());
        assertThat(resultUpdateContent.questionOpenSaved().getText()).isEqualTo(resultUpdateContent.data().questionDtoOpen().text());
        assertThat(resultUpdateContent.questionOpenSaved().getPoint()).isEqualByComparingTo(resultUpdateContent.data().questionDtoOpen().point());
    }

    @Test
    @DisplayName("Создание вариантов ответа для одиночного вопроса в пустом тесте")
    void updateTestContent_ShouldCreateAnswerOptionsForSingleChoice_WhenTestIsEmpty() {
        UpdateContentResult resultUpdateContent = updateEmptyTestAndCollectResult();

        assertThat(answerOptionRepo.count()).isEqualTo(resultUpdateContent.initialAnswerOptionCount() + 2);
        assertThat(resultUpdateContent.answerOptionsSingleSaved()).hasSize(2);
        assertThat(resultUpdateContent.answerOptionsSingleSaved())
                .extracting(AnswerOption::getText)
                .containsExactlyInAnyOrder(
                        resultUpdateContent.data().answerOptionDtoFirst().text(),
                        resultUpdateContent.data().answerOptionDtoSecond().text()
                );
    }

    @Test
    @DisplayName("Создание вопроса с открытым ответом без вариантов в пустом тесте")
    void updateTestContent_ShouldSaveOpenTextAnswersWithoutAnswerOptions_WhenTestIsEmpty() {
        UpdateContentResult resultUpdateContent = updateEmptyTestAndCollectResult();

        assertThat(resultUpdateContent.questionOpenSaved().getCorrectTextAnswers())
                .containsExactlyInAnyOrderElementsOf(resultUpdateContent.data().correctTextAnswers());

        List<AnswerOption> answerOptionsByQuestionOpen = answerOptionRepo
                .findAllByIdsQuestionWithQuestion(Set.of(resultUpdateContent.questionOpenSaved().getId()));
        assertThat(answerOptionsByQuestionOpen).isEmpty();
    }

    @Test
    @DisplayName("Обновление существующего вопроса при изменении контента теста с данными")
    void updateTestContent_ShouldUpdateExistingQuestion_WhenTestHasContent() {
        UpdateExistingContentResult resultUpdateExisting = updateExistingTestAndCollectResult();

        assertThat(questionRepo.count()).isEqualTo(resultUpdateExisting.initialQuestionCount());
        assertThat(resultUpdateExisting.questionsFinal()).hasSize(2);
        assertThat(resultUpdateExisting.questionUpdatedFinal().getText()).isEqualTo(resultUpdateExisting.data().questionDtoUpdated().text());
        assertThat(resultUpdateExisting.questionUpdatedFinal().getType()).isEqualTo(Type.MULTIPLE_CHOICE);
        assertThat(resultUpdateExisting.questionUpdatedFinal().getPoint()).isEqualByComparingTo(resultUpdateExisting.data().questionDtoUpdated().point());
    }

    @Test
    @DisplayName("Обновление и добавление вариантов ответа у существующего вопроса в тесте с данными")
    void updateTestContent_ShouldUpdateAndAddAnswerOptions_WhenTestHasContent() {
        UpdateExistingContentResult resultUpdateExisting = updateExistingTestAndCollectResult();

        // Было два варианта, один удалили и два добавили, итоговое изменение +1
        assertThat(answerOptionRepo.count()).isEqualTo(resultUpdateExisting.initialAnswerOptionCount() + 1);
        assertThat(resultUpdateExisting.answerOptionsUpdatedFinal()).hasSize(2);

        AnswerOption answerOptionUpdatedFinal = resultUpdateExisting.answerOptionsUpdatedFinal().stream()
                .filter(answerOption -> answerOption.getId().equals(resultUpdateExisting.data().idAnswerOptionExistingFirst()))
                .findFirst()
                .orElseThrow();
        assertThat(answerOptionUpdatedFinal.getText()).isEqualTo(resultUpdateExisting.data().answerOptionDtoUpdated().text());
        assertThat(answerOptionUpdatedFinal.getExplanation()).isEqualTo(resultUpdateExisting.data().answerOptionDtoUpdated().explanation());

        AnswerOption answerOptionNewFinal = resultUpdateExisting.answerOptionsUpdatedFinal().stream()
                .filter(answerOption -> !answerOption.getId().equals(resultUpdateExisting.data().idAnswerOptionExistingFirst()))
                .findFirst()
                .orElseThrow();
        assertThat(answerOptionNewFinal.getText()).isEqualTo(resultUpdateExisting.data().answerOptionDtoNew().text());
        assertThat(answerOptionNewFinal.getExplanation()).isEqualTo(resultUpdateExisting.data().answerOptionDtoNew().explanation());

        assertThat(answerOptionRepo.findById(resultUpdateExisting.data().idAnswerOptionExistingSecond())).isEmpty();
    }

    @Test
    @DisplayName("Добавление нового вопроса с вариантами при обновлении контента теста с данными")
    void updateTestContent_ShouldAddNewQuestionWithAnswerOptions_WhenTestHasContent() {
        UpdateExistingContentResult resultUpdateExisting = updateExistingTestAndCollectResult();

        assertThat(resultUpdateExisting.questionNewFinal().getText()).isEqualTo(resultUpdateExisting.data().questionDtoNew().text());
        assertThat(resultUpdateExisting.questionNewFinal().getType()).isEqualTo(Type.SINGLE_CHOICE);
        assertThat(resultUpdateExisting.questionNewFinal().getPoint()).isEqualByComparingTo(resultUpdateExisting.data().questionDtoNew().point());

        assertThat(resultUpdateExisting.answerOptionsNewQuestionFinal()).hasSize(1);
        assertThat(resultUpdateExisting.answerOptionsNewQuestionFinal().getFirst().getText())
                .isEqualTo(resultUpdateExisting.data().answerOptionDtoNewQuestion().text());

        assertThat(questionRepo.findById(resultUpdateExisting.data().idQuestionExistingSecond())).isEmpty();
    }

    /**
     * Создает пустой тест, собирает DTO для обновления контента, вызывает updateTestContent и возвращает срез данных
     * для последующих утверждений в тестах, связанных с добавлением вопросов и вариантов в пустой тест.
     */
    private UpdateContentResult updateEmptyTestAndCollectResult() {
        TestModel testNew = createTest("Test Title", "Test Description", Status.PUBLIC, author);
        testNew = testRepo.save(testNew);
        setSecurityContext(author);

        UpdateTestContentData dataUpdateContent = createUpdateTestContentData(testNew.getId());
        long initialQuestionCount = questionRepo.count();
        long initialAnswerOptionCount = answerOptionRepo.count();

        testCommandService.updateTestContent(dataUpdateContent.updateDto());

        List<Question> questionsSaved = questionRepo.findByTestId(testNew.getId());
        Question questionSingleSaved = findSingleChoiceQuestion(questionsSaved);
        List<AnswerOption> answerOptionsSingleSaved = answerOptionRepo
                .findAllByIdsQuestionWithQuestion(Set.of(questionSingleSaved.getId()));
        Question questionOpenSaved = findOpenTextQuestion(questionsSaved);

        return new UpdateContentResult(
                dataUpdateContent,
                initialQuestionCount,
                initialAnswerOptionCount,
                questionsSaved,
                questionSingleSaved,
                answerOptionsSingleSaved,
                questionOpenSaved
        );
    }

    /**
     * Формирует DTO для обновления пустого теста: один вопрос с вариантами и один вопрос с открытым ответом,
     * а также возвращает сопутствующие данные (answer options, тексты) для последующей валидации.
     */
    private UpdateTestContentData createUpdateTestContentData(Long idTest) {
        TestUpdateTestContentDto.AnswerOptionDto answerOptionDtoFirst = createAnswerOptionDtoNew();
        TestUpdateTestContentDto.AnswerOptionDto answerOptionDtoSecond = createAnswerOptionDtoNew();

        TestUpdateTestContentDto.QuestionDto questionDtoSingle = createQuestionDtoSingleChoice(
                List.of(answerOptionDtoFirst, answerOptionDtoSecond)
        );

        List<String> correctTextAnswers = List.of("Correct Answer One", "Correct Answer Two");
        TestUpdateTestContentDto.QuestionDto questionDtoOpen = createQuestionDtoOpenText(correctTextAnswers);

        TestUpdateTestContentDto updateDtoNew = new TestUpdateTestContentDto(
                idTest,
                List.of(questionDtoSingle, questionDtoOpen)
        );

        return new UpdateTestContentData(
                updateDtoNew,
                questionDtoSingle,
                questionDtoOpen,
                answerOptionDtoFirst,
                answerOptionDtoSecond,
                correctTextAnswers
        );
    }

    /**
     * Возвращает первый вопрос с типом SINGLE_CHOICE из списка сохраненных.
     */
    private Question findSingleChoiceQuestion(List<Question> questionsSaved) {
        return questionsSaved.stream()
                .filter(question -> question.getType() == Type.SINGLE_CHOICE)
                .findFirst()
                .orElseThrow();
    }

    /**
     * Возвращает первый вопрос с типом OPEN_TEXT из списка сохраненных.
     */
    private Question findOpenTextQuestion(List<Question> questionsSaved) {
        return questionsSaved.stream()
                .filter(question -> question.getType() == Type.OPEN_TEXT)
                .findFirst()
                .orElseThrow();
    }

    /**
     * Создает тест с начальными вопросами и вариантами, формирует DTO с обновлением (изменение существующего и добавление нового вопроса),
     * вызывает updateTestContent и агрегирует полученное состояние для проверок в тестах сценария обновления с контентом.
     */
    private UpdateExistingContentResult updateExistingTestAndCollectResult() {
        ExistingTestData dataExistingTest = createExistingTestData();
        setSecurityContext(author);

        UpdateExistingTestData dataUpdateExisting = createUpdateExistingTestData(dataExistingTest);
        long initialQuestionCount = questionRepo.count();
        long initialAnswerOptionCount = answerOptionRepo.count();

        testCommandService.updateTestContent(dataUpdateExisting.updateDtoExisting());

        List<Question> questionsFinal = questionRepo.findByTestId(dataExistingTest.testExisting().getId());
        Question questionUpdatedFinal = findQuestionById(questionsFinal, dataUpdateExisting.idQuestionExistingFirst());
        List<AnswerOption> answerOptionsUpdatedFinal = answerOptionRepo
                .findAllByIdsQuestionWithQuestion(Set.of(questionUpdatedFinal.getId()));
        Question questionNewFinal = findQuestionExcludingId(questionsFinal, dataUpdateExisting.idQuestionExistingFirst());
        List<AnswerOption> answerOptionsNewQuestionFinal = answerOptionRepo
                .findAllByIdsQuestionWithQuestion(Set.of(questionNewFinal.getId()));

        return new UpdateExistingContentResult(
                dataUpdateExisting,
                initialQuestionCount,
                initialAnswerOptionCount,
                questionsFinal,
                questionUpdatedFinal,
                answerOptionsUpdatedFinal,
                questionNewFinal,
                answerOptionsNewQuestionFinal
        );
    }

    /**
     * Подготавливает тест с вопросами и вариантами для сценария обновления: один single-choice с двумя вариантами
     * и один open-text с корректными ответами. Возвращает собранные сущности и данные для дальнейших шагов.
     */
    private ExistingTestData createExistingTestData() {
        TestModel testExisting = createTestRadom(author);
        testExisting = testRepo.save(testExisting);

        Question questionExistingFirst = createQuestionRandom(Type.SINGLE_CHOICE);
        questionExistingFirst.setTest(testExisting);
        questionExistingFirst = questionRepo.save(questionExistingFirst);

        AnswerOption answerOptionExistingFirst = createAnswerOptionRandom(questionExistingFirst);
        answerOptionExistingFirst = answerOptionRepo.save(answerOptionExistingFirst);
        questionExistingFirst.getAnswerOptions().add(answerOptionExistingFirst);

        AnswerOption answerOptionExistingSecond = createAnswerOptionRandom(questionExistingFirst);
        answerOptionExistingSecond = answerOptionRepo.save(answerOptionExistingSecond);
        questionExistingFirst.getAnswerOptions().add(answerOptionExistingSecond);

        Question questionExistingSecond = createQuestionRandom(Type.OPEN_TEXT);
        questionExistingSecond.setTest(testExisting);
        List<String> correctTextAnswersExisting = List.of(getRandomString(10));
        questionExistingSecond.setCorrectTextAnswers(correctTextAnswersExisting);
        questionExistingSecond = questionRepo.save(questionExistingSecond);

        testExisting.getQuestions().add(questionExistingFirst);
        testExisting.getQuestions().add(questionExistingSecond);
        testRepo.save(testExisting);

        return new ExistingTestData(
                testExisting,
                questionExistingFirst,
                questionExistingSecond,
                answerOptionExistingFirst,
                answerOptionExistingSecond,
                correctTextAnswersExisting
        );
    }

    /**
     * На базе подготовленного теста с контентом собирает DTO для обновления: обновление первого вопроса (в multiple-choice,
     * один вариант обновить, один добавить) и добавление нового single-choice вопроса с вариантом. Также возвращает
     * идентификаторы исходных сущностей для дальнейшей проверки удаления/обновления.
     */
    private UpdateExistingTestData createUpdateExistingTestData(ExistingTestData dataExistingTest) {
        Long idAnswerOptionExistingFirst = dataExistingTest.answerOptionExistingFirst().getId();
        TestUpdateTestContentDto.AnswerOptionDto answerOptionDtoUpdated = createAnswerOptionDtoExisting(idAnswerOptionExistingFirst);
        TestUpdateTestContentDto.AnswerOptionDto answerOptionDtoNew = createAnswerOptionDtoNew();

        Long idQuestionExistingFirst = dataExistingTest.questionExistingFirst().getId();
        TestUpdateTestContentDto.QuestionDto questionDtoUpdated = createQuestionDtoExistingMultipleChoice(
                idQuestionExistingFirst,
                List.of(answerOptionDtoUpdated, answerOptionDtoNew)
        );

        TestUpdateTestContentDto.AnswerOptionDto answerOptionDtoNewQuestion = createAnswerOptionDtoNew();
        TestUpdateTestContentDto.QuestionDto questionDtoNew = createQuestionDtoSingleChoice(
                List.of(answerOptionDtoNewQuestion)
        );

        TestUpdateTestContentDto updateDtoExisting = new TestUpdateTestContentDto(
                dataExistingTest.testExisting().getId(),
                List.of(questionDtoUpdated, questionDtoNew)
        );

        return new UpdateExistingTestData(
                updateDtoExisting,
                questionDtoUpdated,
                questionDtoNew,
                answerOptionDtoUpdated,
                answerOptionDtoNew,
                answerOptionDtoNewQuestion,
                idAnswerOptionExistingFirst,
                dataExistingTest.answerOptionExistingSecond().getId(),
                idQuestionExistingFirst,
                dataExistingTest.questionExistingSecond().getId()
        );
    }

    /**
     * Ищет вопрос по идентификатору в списке.
     */
    private Question findQuestionById(List<Question> questions, Long idQuestion) {
        return questions.stream()
                .filter(question -> question.getId().equals(idQuestion))
                .findFirst()
                .orElseThrow();
    }

    /**
     * Возвращает вопрос, идентификатор которого отличается от указанного.
     */
    private Question findQuestionExcludingId(List<Question> questions, Long idExcluded) {
        return questions.stream()
                .filter(question -> !question.getId().equals(idExcluded))
                .findFirst()
                .orElseThrow();
    }

    /**
     * Устанавливает SecurityContext для переданного пользователя.
     */
    private void setSecurityContext(User userCurrent) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userCurrent,
                null,
                userCurrent.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    /**
     * Данные для обновления пустого теста: итоговое DTO, оба вопроса, варианты и корректные ответы для открытого вопроса.
     * Используется при сборке входных данных и проверках для сценария пустого теста.
     */
    private record UpdateTestContentData(
            TestUpdateTestContentDto updateDto,
            TestUpdateTestContentDto.QuestionDto questionDtoSingle,
            TestUpdateTestContentDto.QuestionDto questionDtoOpen,
            TestUpdateTestContentDto.AnswerOptionDto answerOptionDtoFirst,
            TestUpdateTestContentDto.AnswerOptionDto answerOptionDtoSecond,
            List<String> correctTextAnswers
    ) {
    }

    /**
     * Результат выполнения updateTestContent для пустого теста: исходные счетчики, сохраненные вопросы и варианты,
     * а также вспомогательные ссылки на сохраненные сущности для последующих утверждений.
     */
    private record UpdateContentResult(
            UpdateTestContentData data,
            long initialQuestionCount,
            long initialAnswerOptionCount,
            List<Question> questionsSaved,
            Question questionSingleSaved,
            List<AnswerOption> answerOptionsSingleSaved,
            Question questionOpenSaved
    ) {
    }

    /**
     * Исходные сущности теста с контентом для сценария обновления: тест, два вопроса, два варианта ответа,
     * корректные ответы для open-text. Используется для подготовки и валидации обновления.
     */
    private record ExistingTestData(
            TestModel testExisting,
            Question questionExistingFirst,
            Question questionExistingSecond,
            AnswerOption answerOptionExistingFirst,
            AnswerOption answerOptionExistingSecond,
            List<String> correctTextAnswersExisting
    ) {
    }

    /**
     * DTO и идентификаторы для обновления теста с контентом: содержит обновляемый и новый вопросы, обновляемый
     * и новые варианты, а также id исходных сущностей для проверки обновления и удаления.
     */
    private record UpdateExistingTestData(
            TestUpdateTestContentDto updateDtoExisting,
            TestUpdateTestContentDto.QuestionDto questionDtoUpdated,
            TestUpdateTestContentDto.QuestionDto questionDtoNew,
            TestUpdateTestContentDto.AnswerOptionDto answerOptionDtoUpdated,
            TestUpdateTestContentDto.AnswerOptionDto answerOptionDtoNew,
            TestUpdateTestContentDto.AnswerOptionDto answerOptionDtoNewQuestion,
            Long idAnswerOptionExistingFirst,
            Long idAnswerOptionExistingSecond,
            Long idQuestionExistingFirst,
            Long idQuestionExistingSecond
    ) {
    }

    /**
     * Агрегированный результат выполнения updateTestContent для теста с контентом: исходные счетчики, финальные вопросы
     * и варианты, а также ссылки на обновленный и добавленный вопрос для целевых проверок.
     */
    private record UpdateExistingContentResult(
            UpdateExistingTestData data,
            long initialQuestionCount,
            long initialAnswerOptionCount,
            List<Question> questionsFinal,
            Question questionUpdatedFinal,
            List<AnswerOption> answerOptionsUpdatedFinal,
            Question questionNewFinal,
            List<AnswerOption> answerOptionsNewQuestionFinal
    ) {
    }
}
