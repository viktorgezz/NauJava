package ru.viktorgezz.NauJava.domain.test.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.domain.answer_option.service.intrf.AnswerOptionCommandService;
import ru.viktorgezz.NauJava.domain.answer_option.service.intrf.AnswerOptionQueryService;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.question.service.QuestionCommandService;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.dto.TestMetadataRequestDto;
import ru.viktorgezz.NauJava.domain.test.dto.TestUpdateTestContentDto;
import ru.viktorgezz.NauJava.domain.test.repo.TestRepo;
import ru.viktorgezz.NauJava.domain.test.service.intrf.TestCommandService;
import ru.viktorgezz.NauJava.domain.topic.Topic;
import ru.viktorgezz.NauJava.domain.topic.service.TopicService;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.exception.BusinessException;
import ru.viktorgezz.NauJava.exception.ErrorCode;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static ru.viktorgezz.NauJava.security.util.CurrentUserUtils.getCurrentUser;

/**
 * Реализация сервиса для управления тестами. Реализует {@link TestCommandService}.
 */
@Service
public class TestCommandServiceImpl implements TestCommandService {

    private final TestRepo testRepo;

    private final TopicService topicService;
    private final AnswerOptionCommandService answerOptionCommandService;
    private final AnswerOptionQueryService answerOptionQueryService;
    private final QuestionCommandService questionCommandService;

    @Autowired
    public TestCommandServiceImpl(
            TestRepo testRepo,
            TopicService topicService,
            AnswerOptionCommandService answerOptionCommandService,
            AnswerOptionQueryService answerOptionQueryService,
            QuestionCommandService questionCommandService
    ) {
        this.testRepo = testRepo;
        this.topicService = topicService;
        this.answerOptionCommandService = answerOptionCommandService;
        this.answerOptionQueryService = answerOptionQueryService;
        this.questionCommandService = questionCommandService;
    }

    @Override
    @Transactional
    public Long updateTestMetadata(TestMetadataRequestDto testDto) {
        TestModel testNew = new TestModel();
        if (testDto.idTest() != null) {
            Optional<TestModel> testOptional = testRepo.findForEditingMetadata(testDto.idTest());
            testNew = getValidatedTest(testOptional);
        }
        testNew.setTitle(testDto.title());
        testNew.setDescription(testDto.description());
        testNew.setStatus(testDto.status());

        if (testNew.getAuthor() == null) {
            testNew.setAuthor(getCurrentUser());
        }

        List<Topic> topicsActual = topicService.findOrCreateTopics(testDto.titlesTopic());
        List<Topic> topicsToRemove = testNew.getTopics().stream().filter(topic -> !topicsActual.contains(topic)).toList();
        topicsToRemove.forEach(testNew::removeTopic);
        topicsActual.forEach(testNew::addTopic);

        topicService.saveAll(topicsActual);
        TestModel testSaved = testRepo.save(testNew);

        return testSaved.getId();
    }

    @Override
    @Transactional
    public void updateTestContent(TestUpdateTestContentDto testDto) {
        Optional<TestModel> testOptional = testRepo.findForEditingContent(testDto.idTest());
        TestModel testExisting = getValidatedTest(testOptional);

        // Создаем мапы для быстрого поиска существующих вопросов и вариантов ответов по их ID
        // Это позволяет эффективно определять, какие сущности нужно обновить, а какие создать/удалить
        Map<Long, Question> questionsExistingMap = testExisting
                .getQuestions()
                .stream()
                .filter(question -> question.getId() != null)
                .collect(Collectors.toMap(Question::getId, question -> question));

        Set<Long> idsQuestions = questionsExistingMap.keySet();
        Map<Long, AnswerOption> answerOptionsExistingMap = answerOptionQueryService.findAllAnswerOptionByIdsQuestionWithQuestion(idsQuestions)
                .stream()
                .filter(answerOption -> answerOption.getId() != null)
                .collect(Collectors.toMap(AnswerOption::getId, answerOption -> answerOption));

        Set<Long> idsChangedQuestion = new HashSet<>();

        // Обрабатываем вопросы из DTO: обновляем существующие или создаем новые
        testDto.questions().forEach(questionDto -> {
            Question questionExisting = Optional.ofNullable(questionDto.idQuestion())
                    .map(questionsExistingMap::get)
                    .orElse(null);

            if (questionExisting != null) {
                // Обновляем существующий вопрос: меняем его поля
                questionCommandService.updateQuestionWithoutSave(questionExisting, questionDto);

                Set<Long> idsChangedAnswerOption = new HashSet<>();
                List<AnswerOption> newlyCreatedAnswerOptions = new ArrayList<>();

                // Обрабатываем варианты ответов для существующего вопроса
                if (questionDto.answerOptions() != null) {
                    questionDto.answerOptions().forEach(answerOptionDto -> {
                        Long answerOptionId = answerOptionDto.idAnswerOption();
                        if (answerOptionId != null && answerOptionsExistingMap.containsKey(answerOptionId)) {
                            // Найден существующий: Обновляем и добавляем ID в список измененных
                            AnswerOption existing = answerOptionsExistingMap.get(answerOptionId);
                            answerOptionCommandService.updateAnswerOptionWithoutSave(existing, answerOptionDto);
                            idsChangedAnswerOption.add(answerOptionId);
                        } else {
                            // Не найден (ID был null или не нашелся в мапе): Создаем новый
                            AnswerOption newAnswerOption = answerOptionCommandService.createAndLinkAnswerOption(
                                    questionExisting, answerOptionDto);
                            newlyCreatedAnswerOptions.add(newAnswerOption);
                        }
                    });
                }

                // Удаляем варианты ответов, которых нет в DTO (они были удалены из вопроса)
                // Учитываем только существующие AnswerOption с ID (новые созданные не удаляем)
                List<AnswerOption> answerOptionsToRemove = questionExisting
                        .getAnswerOptions()
                        .stream()
                        .filter(answerOption -> answerOption.getId() != null)
                        .filter(answerOption -> !idsChangedAnswerOption.contains(answerOption.getId()))
                        .filter(answerOption -> !newlyCreatedAnswerOptions.contains(answerOption))
                        .toList();
                questionExisting.getAnswerOptions().removeAll(answerOptionsToRemove);

                // Сохраняем обновленные и новые варианты ответов
                answerOptionCommandService.saveAll(questionExisting.getAnswerOptions());

                idsChangedQuestion.add(questionExisting.getId());
            } else {
                // Создаем новый вопрос
                Question questionNew = questionCommandService.createQuestionWithoutSave(questionDto, testExisting);

                List<AnswerOption> answerOptionsNew = new ArrayList<>();
                // Создаем варианты ответов для нового вопроса
                if (questionDto.answerOptions() != null) {
                    questionDto.answerOptions().forEach(answerOptionDto -> {
                        AnswerOption answerOptionNew = answerOptionCommandService.createAndLinkAnswerOption(questionNew, answerOptionDto);
                        answerOptionsNew.add(answerOptionNew);
                    });
                }

                answerOptionCommandService.saveAll(answerOptionsNew);
                testExisting.getQuestions().add(questionNew);
            }
        });

        // Удаляем вопросы, которых нет в DTO (они были удалены из теста)
        List<Question> questionsToRemove = testExisting
                .getQuestions()
                .stream()
                .filter(question -> question.getId() != null)
                .filter(question -> !idsChangedQuestion.contains(question.getId()))
                .toList();
        testExisting.getQuestions().removeAll(questionsToRemove);
        questionCommandService.saveAll(testExisting.getQuestions());

        // Обновляем максимальное количество всех балов за тест
        final BigDecimal pointSumMax = testExisting.getQuestions()
                .stream()
                .map(Question::getPoint)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        testExisting.setScoreMax(pointSumMax);

        // Сохраняем тест
        testRepo.save(testExisting);
    }

    private TestModel getValidatedTest(Optional<TestModel> test) {
        TestModel testModel = test.orElseThrow(
                () -> new EntityNotFoundException("Test not found"));

        User author = testModel.getAuthor();
        if (author == null) {
            throw new IllegalStateException("Test author is null");
        }

        Long idOwner = author.getId();
        Long idCurrentUser = getCurrentUser().getId();

        if (!idOwner.equals(idCurrentUser)) {
            throw new BusinessException(ErrorCode.USER_FORBIDDEN, idCurrentUser);
        }

        return testModel;
    }
}
