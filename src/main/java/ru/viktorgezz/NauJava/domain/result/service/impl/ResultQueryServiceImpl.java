package ru.viktorgezz.NauJava.domain.result.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.domain.answer_option.service.intrf.AnswerOptionQueryService;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.result.dto.ResultResponseDto;
import ru.viktorgezz.NauJava.domain.result.repo.ResultRepo;
import ru.viktorgezz.NauJava.domain.result.service.intrf.ResultQueryService;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.user_answer.UserAnswer;
import ru.viktorgezz.NauJava.domain.user_answer.service.UserAnswerService;
import ru.viktorgezz.NauJava.exception.BusinessException;
import ru.viktorgezz.NauJava.exception.ErrorCode;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ru.viktorgezz.NauJava.domain.util.GroupingUtil.*;

/**
 * Реализация сервиса, который читает данные {@link Result}. Реализует {@link ResultQueryService}.
 */
@Service
public class ResultQueryServiceImpl implements ResultQueryService {

    private final ResultRepo resultRepo;
    private final AnswerOptionQueryService answerOptionQueryService;
    private final UserAnswerService userAnswerService;

    @Autowired
    public ResultQueryServiceImpl(
            ResultRepo resultRepo,
            AnswerOptionQueryService answerOptionQueryService,
            UserAnswerService userAnswerService
    ) {
        this.resultRepo = resultRepo;
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
}
