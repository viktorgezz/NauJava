package ru.viktorgezz.NauJava.domain.result.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.domain.answer_option.service.intrf.AnswerOptionQueryService;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.result.dto.ResultMetadataResponseDto;
import ru.viktorgezz.NauJava.domain.result.dto.ResultResponseDto;
import ru.viktorgezz.NauJava.domain.result.dto.ResultShortMetadataResponseDto;
import ru.viktorgezz.NauJava.domain.result.repo.ResultPagingRepo;
import ru.viktorgezz.NauJava.domain.result.repo.ResultRepo;
import ru.viktorgezz.NauJava.domain.result.service.intrf.ResultQueryService;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user_answer.UserAnswer;
import ru.viktorgezz.NauJava.domain.user_answer.service.UserAnswerService;
import ru.viktorgezz.NauJava.exception.BusinessException;
import ru.viktorgezz.NauJava.exception.ErrorCode;

import java.util.*;

import static ru.viktorgezz.NauJava.domain.util.GroupingUtil.*;
import static ru.viktorgezz.NauJava.security.util.CurrentUserUtils.*;

/**
 * Реализация сервиса, который читает данные {@link Result}. Реализует {@link ResultQueryService}.
 */
@Service
public class ResultQueryServiceImpl implements ResultQueryService {

    private final ResultRepo resultRepo;
    private final ResultPagingRepo resultPagingRepo;
    private final AnswerOptionQueryService answerOptionQueryService;
    private final UserAnswerService userAnswerService;

    @Autowired
    public ResultQueryServiceImpl(
            ResultRepo resultRepo,
            ResultPagingRepo resultPagingRepo,
            AnswerOptionQueryService answerOptionQueryService,
            UserAnswerService userAnswerService
    ) {
        this.resultRepo = resultRepo;
        this.resultPagingRepo = resultPagingRepo;
        this.answerOptionQueryService = answerOptionQueryService;
        this.userAnswerService = userAnswerService;
    }

    @Override
    public ResultResponseDto getTestResultDto(Long id) {
        Result resultFound = resultRepo.findByIdWithTestAndQuestions(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESULT_NOT_FOUND, id));
        TestModel test = resultFound.getTest();

        List<Question> questions = test.getQuestions();
        List<Long> idsQuestion = extractQuestionIds(questions);

        Map<Long, List<AnswerOption>> idQuestionToAnswerOptions = buildAnswerOptionMapByQuestionId(
                answerOptionQueryService
                        .findAllAnswerOptionByIdsQuestionWithQuestion(idsQuestion));
        Map<Long, List<UserAnswer>> idQuestionToUserAnswers = buildUserAnswerMapByQuestionId(
                userAnswerService.findAllByIdsQuestionAndResultIdWithQuestionAndAnswerOption(idsQuestion, id));

        List<ResultResponseDto.QuestionDto> questionsDto = questions
                .stream()
                .map(question -> {
                    List<AnswerOption> answerOptionsCurrent = idQuestionToAnswerOptions.getOrDefault(question.getId(), Collections.emptyList());
                    List<UserAnswer> userAnswersCurrent = idQuestionToUserAnswers.getOrDefault(question.getId(), Collections.emptyList());

                    Set<Long> idsSelected = extractIdsAnswerOptionFromUserAnswers(userAnswersCurrent);
                    String userTextAnswer = extractTextAnswerFromUserAnswers(userAnswersCurrent);

                    List<ResultResponseDto.UserAnswerResponseDto> userAnswersDto = buildUserAnswersResponseDto(
                            answerOptionsCurrent,
                            idsSelected,
                            userTextAnswer,
                            question
                    );

                    return new ResultResponseDto.QuestionDto(
                            question.getId(),
                            question.getText(),
                            question.getType(),
                            question.isAllowMistakes(),
                            userAnswersDto
                    );
                })
                .toList();

        return new ResultResponseDto(
                id,
                test.getId(),
                resultFound.getScore(),
                test.getScoreMax(),
                resultFound.getGrade(),
                resultFound.getTimeSpentSeconds(),
                resultFound.getCompletedAt(),
                questionsDto
        );
    }

    @Override
    public List<Result> findAllWithParticipantUsernameAndTitleTest() {
        return resultRepo.findAllWithParticipantUsernameAndTitleTest();
    }

    @Override
    public Page<ResultMetadataResponseDto> findUserResults(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Long> idsPage = resultPagingRepo.findAllUserResults(currentUser.getId(), pageable);

        if (idsPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Result> results = resultRepo.findAllWithTestByIds(idsPage.getContent());

        List<Long> orderedIds = idsPage.getContent();
        results.sort(Comparator.comparingInt(result -> orderedIds.indexOf(result.getId())));

        List<ResultMetadataResponseDto> resultsDto = results.stream()
                .map(result -> {
                    TestModel test = result.getTest();
                    return new ResultMetadataResponseDto(
                            result.getId(),
                            test.getId(),
                            test.getStatus(),
                            test.getTitle(),
                            result.getScore(),
                            test.getScoreMax(),
                            result.getCompletedAt(),
                            result.getTimeSpentSeconds()
                    );
                })
                .toList();

        return new PageImpl<>(resultsDto, pageable, idsPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResultShortMetadataResponseDto> findResultLastThreeAttempts(Long idTest) {
        Long idUser = getCurrentUser().getId();
        if (idUser == null) {
            return Collections.emptyList();
        }
        return resultRepo.findResultLastAttempts(idTest, getCurrentUser().getId())
                .limit(3)
                .map(result ->
                        new ResultShortMetadataResponseDto(
                                result.getScore(),
                                result.getTest().getScoreMax(),
                                result.getTimeSpentSeconds()
                        ))
                .toList();
    }
}
