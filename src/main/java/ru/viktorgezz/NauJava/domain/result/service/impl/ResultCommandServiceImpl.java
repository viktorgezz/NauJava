package ru.viktorgezz.NauJava.domain.result.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.domain.answer_option.service.intrf.AnswerOptionQueryService;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.result.Grade;
import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.result.dto.ResultRequestDto;
import ru.viktorgezz.NauJava.domain.result.repo.ResultRepo;
import ru.viktorgezz.NauJava.domain.result.service.intrf.ResultCommandService;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.service.intrf.TestQueryService;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user_answer.UserAnswer;
import ru.viktorgezz.NauJava.domain.user_answer.service.UserAnswerService;
import ru.viktorgezz.NauJava.security.util.CurrentUserUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static ru.viktorgezz.NauJava.domain.result.service.util.ResultScoringCalculator.calculateTotalScoreAndCollectUserAnswers;
import static ru.viktorgezz.NauJava.domain.util.GroupingUtil.buildAnswerOptionMapByQuestionId;
import static ru.viktorgezz.NauJava.domain.util.GroupingUtil.extractQuestionIds;

/**
 * Реализация сервиса управления результатами тестов {@link Result}. Реализует {@link ResultCommandService}.
 */
@Service
public class ResultCommandServiceImpl implements ResultCommandService {

    public static final String MOSCOW_ZONE_ID = "Europe/Moscow";

    private final ResultRepo resultRepo;
    private final TestQueryService testQueryService;
    private final UserAnswerService userAnswerService;
    private final AnswerOptionQueryService answerOptionQueryService;


    @Autowired
    public ResultCommandServiceImpl(
            ResultRepo resultRepo,
            TestQueryService testQueryService,
            UserAnswerService userAnswerService,
            AnswerOptionQueryService answerOptionQueryService
    ) {
        this.resultRepo = resultRepo;
        this.testQueryService = testQueryService;
        this.userAnswerService = userAnswerService;
        this.answerOptionQueryService = answerOptionQueryService;
    }

    @Override
    @Transactional
    public Long initiateCompilationResult(ResultRequestDto resultRequestDto) {
        TestModel test = testQueryService.findById(resultRequestDto.idTest());
        User user = CurrentUserUtils.getCurrentUser();
        Result result = new Result(
                resultRequestDto.timeSpentSeconds(),
                LocalDateTime.now(ZoneId.of(MOSCOW_ZONE_ID)),
                user,
                test
        );

        test.getTestResults().add(result);

        return resultRepo.save(result).getId();
    }

    @Override
    @Transactional
    public CompletableFuture<Result> compilateResultAsync(
            Map<Long, ResultRequestDto.UserAnswerRequestDto> idQuestionToUserAnswer,
            Long idResult
    ) {
        Result resultCompilated = resultRepo.findByIdWithTestAndQuestions(idResult)
                .orElseThrow(EntityNotFoundException::new);
        TestModel testCurr = resultCompilated.getTest();

        final BigDecimal scoreMax = testCurr.getScoreMax();
        List<Question> questions = testCurr.getQuestions();

        List<Long> idsQuestion = extractQuestionIds(questions);
        List<AnswerOption> answerOptions = answerOptionQueryService
                .findAllAnswerOptionByIdsQuestionWithQuestion(idsQuestion);
        Map<Long, List<AnswerOption>> idQuestionToAnswerOptions =
                buildAnswerOptionMapByQuestionId(answerOptions);

        List<UserAnswer> userAnswersProcessed = new ArrayList<>();
        BigDecimal scoreCommon = calculateTotalScoreAndCollectUserAnswers(
                questions,
                idQuestionToUserAnswer,
                idQuestionToAnswerOptions,
                resultCompilated,
                userAnswersProcessed
        );

        List<UserAnswer> userAnswersSaved = userAnswerService.saveAll(userAnswersProcessed);

        resultCompilated.setScore(scoreCommon);
        resultCompilated.setGrade(Grade.calculateGrade(scoreCommon, scoreMax));
        resultCompilated.getUserAnswers().addAll(userAnswersSaved);
        Result resultSaved = resultRepo.save(resultCompilated);

        return CompletableFuture.completedFuture(resultSaved);
    }

    @Override
    @Transactional
    public void deleteResult(Long idResult) {
        if (!resultRepo.existsById(idResult)) {
            throw new EntityNotFoundException("Result with idTest " + idResult + " not found.");
        }
        resultRepo.deleteById(idResult);
    }
}
