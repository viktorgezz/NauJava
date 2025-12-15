package ru.viktorgezz.NauJava.domain.test.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.domain.answer_option.service.intrf.AnswerOptionQueryService;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.dto.TestToPassDto;
import ru.viktorgezz.NauJava.domain.test.dto.TestUpdateContentDto;
import ru.viktorgezz.NauJava.domain.test.repo.TestPagingAndSortingRepo;
import ru.viktorgezz.NauJava.domain.test.repo.TestRepo;
import ru.viktorgezz.NauJava.domain.test.service.intrf.TestQueryService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static ru.viktorgezz.NauJava.domain.util.GroupingUtil.*;

/**
 * Реализация сервиса чтения тестов. Реализует {@link TestQueryService}.
 */
@Service
public class TestQueryServiceImpl implements TestQueryService {

    private final TestRepo testRepo;
    private final TestPagingAndSortingRepo testPagingAndSortingRepo;
    private final AnswerOptionQueryService answerOptionQueryService;

    @Autowired
    public TestQueryServiceImpl(
            TestRepo testRepo,
            TestPagingAndSortingRepo testPagingAndSortingRepo,
            AnswerOptionQueryService answerOptionQueryService
    ) {
        this.testRepo = testRepo;
        this.testPagingAndSortingRepo = testPagingAndSortingRepo;
        this.answerOptionQueryService = answerOptionQueryService;
    }

    @Override
    public TestModel findById(Long id) {
        return testRepo.findByIdWithAuthor(id).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public TestToPassDto findTestToPassById(Long id) {
        TestModel testFound = testRepo.findForEditingContent(id).orElseThrow(EntityNotFoundException::new);
        List<Question> questions = testFound.getQuestions();
        List<Long> idsQuestion = extractQuestionIds(questions);

        Map<Long, List<AnswerOption>> idQuestionToAnswerOptions = buildAnswerOptionMapByQuestionId(
                answerOptionQueryService
                        .findAllAnswerOptionByIdsQuestionWithQuestion(idsQuestion));

        answerOptionQueryService.findAllAnswerOptionByIdsQuestionWithQuestion(idsQuestion);

        List<TestToPassDto.QuestionDto> questionsDto = buildQuestionDtoTestToPass(
                questions, idQuestionToAnswerOptions);

        return new TestToPassDto(
                testFound.getId(),
                questionsDto
        );
    }

    @Override
    public TestUpdateContentDto findByIdWithContent(Long id) {
        TestModel test = testRepo.findForEditingContent(id).orElseThrow(EntityNotFoundException::new);

        List<Long> idsQuestion = extractQuestionIds(test.getQuestions());
        Map<Long, List<AnswerOption>> idQuestionToAnswerOptions = buildAnswerOptionMapByQuestionId(
                answerOptionQueryService
                        .findAllAnswerOptionByIdsQuestionWithQuestion(idsQuestion)
        );

        List<TestUpdateContentDto.QuestionDto> questionsDto = buildQuestionsDtoTestUpdate(
                test.getQuestions(), idQuestionToAnswerOptions);

        return new TestUpdateContentDto(
                test.getId(),
                questionsDto
        );
    }

    @Override
    public Page<TestModel> findAllWithAuthorAndTopics(Pageable pageable) {
        return getTestsByIdsPage(testPagingAndSortingRepo.findAllTestIds(pageable), pageable);
    }

    @Override
    public Page<TestModel> findByTitle(String title, Pageable pageable) {
        return getTestsByIdsPage(testPagingAndSortingRepo.findTestIdsByTitle(title, pageable), pageable);
    }

    @Override
    public Page<TestModel> findAllByUserIdWithAuthorAndTopics(Long userId, Pageable pageable) {
        return getTestsByIdsPageByUserId(
                testPagingAndSortingRepo.findAllTestIdsByUserId(userId, pageable),
                userId,
                pageable
        );
    }

    @Override
    public Page<TestModel> findByUserIdAndTitle(Long userId, String title, Pageable pageable) {
        return getTestsByIdsPageByUserId(
                testPagingAndSortingRepo.findTestIdsByUserIdAndTitle(userId, title, pageable),
                userId,
                pageable
        );
    }

    private Page<TestModel> getTestsByIdsPage(Page<Long> idsPage, Pageable pageable) {
        if (idsPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<TestModel> tests = testRepo.findAllWithAuthorAndTopicsByIds(idsPage.getContent());

        List<Long> orderedIds = idsPage.getContent();
        tests.sort(Comparator.comparingInt(test -> orderedIds.indexOf(test.getId())));

        return new PageImpl<>(tests, pageable, idsPage.getTotalElements());
    }

    private Page<TestModel> getTestsByIdsPageByUserId(Page<Long> idsPage, Long userId, Pageable pageable) {
        if (idsPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<TestModel> tests = testRepo.findAllWithAuthorAndTopicsByUserIdAndIds(userId, idsPage.getContent());

        List<Long> orderedIds = idsPage.getContent();
        tests.sort(Comparator.comparingInt(test -> orderedIds.indexOf(test.getId())));

        return new PageImpl<>(tests, pageable, idsPage.getTotalElements());
    }
}
