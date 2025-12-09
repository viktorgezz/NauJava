package ru.viktorgezz.NauJava.domain.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.question.Type;
import ru.viktorgezz.NauJava.domain.result.dto.ResultResponseDto;
import ru.viktorgezz.NauJava.domain.test.dto.TestToPassDto;
import ru.viktorgezz.NauJava.domain.test.dto.TestUpdateTestContentDto;
import ru.viktorgezz.NauJava.domain.user_answer.UserAnswer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Утилитный класс для группировки и преобразования данных вопросов, вариантов ответов и ответов пользователей.
 */
public class GroupingUtil {

    private static final Logger log = LoggerFactory.getLogger(GroupingUtil.class);

    /**
     * Извлекает список идентификаторов вопросов.
     *
     * @param questions список вопросов.
     * @return список ID вопросов.
     */
    public static List<Long> extractQuestionIds(List<Question> questions) {
        return questions.stream()
                .map(Question::getId)
                .toList();
    }

    /**
     * Извлекает множество ID выбранных вариантов ответов из ответов пользователя.
     *
     * @param userAnswers список ответов пользователя.
     * @return множество ID выбранных вариантов ответов.
     */
    public static Set<Long> extractIdsAnswerOptionFromUserAnswers(List<UserAnswer> userAnswers) {
        return userAnswers
                .stream()
                .map(userAnswer -> {
                    AnswerOption answerOption = userAnswer.getAnswerOption();
                    if (answerOption != null) {
                        return answerOption.getId();
                    }
                    return null;
                })
                .collect(Collectors.toSet());
    }

    /**
     * Извлекает текстовый ответ пользователя из списка ответов.
     *
     * @param userAnswers список ответов пользователя.
     * @return текстовый ответ или null, если ответа нет.
     */
    public static String extractTextAnswerFromUserAnswers(List<UserAnswer> userAnswers) {
        return userAnswers.stream()
                .map(UserAnswer::getTextAnswer)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * Строит Map соответствия идентификатора вопроса к списку ответов пользователя.
     *
     * @param userAnswers список ответов пользователя.
     * @return Map, где ключ - ID вопроса, значение - список ответов пользователя.
     */
    public static Map<Long, List<UserAnswer>> buildUserAnswerMapByQuestionId(List<UserAnswer> userAnswers) {
        return userAnswers.stream()
                .collect(Collectors.groupingBy(answer -> answer.getQuestion().getId()));
    }

    /**
     * Строит Map соответствия идентификатора вопроса к списку вариантов ответов.
     *
     * @param answerOptions список вариантов ответов.
     * @return Map, где ключ - ID вопроса, значение - список вариантов ответов.
     */
    public static Map<Long, List<AnswerOption>> buildAnswerOptionMapByQuestionId(List<AnswerOption> answerOptions) {
        return answerOptions
                .stream()
                .collect(Collectors.groupingBy(answerOption -> answerOption.getQuestion().getId()));
    }

    /**
     * Строит список DTO ответов пользователя для результата теста в зависимости от типа вопроса.
     *
     * @param answerOptions список вариантов ответов.
     * @param idsSelectedAnswerOption множество ID выбранных вариантов ответов.
     * @param userTextAnswer текстовый ответ пользователя.
     * @param question вопрос.
     * @return список DTO ответов пользователя.
     */
    public static List<ResultResponseDto.UserAnswerResponseDto> buildUserAnswersResponseDto(
            List<AnswerOption> answerOptions,
            Set<Long> idsSelectedAnswerOption,
            String userTextAnswer,
            Question question
    ) {
        Type typeQuestion = question.getType();
        if (typeQuestion.equals(Type.SINGLE_CHOICE) || typeQuestion.equals(Type.MULTIPLE_CHOICE)) {
            return answerOptions
                    .stream()
                    .map(answer -> new ResultResponseDto.UserAnswerResponseDto(
                            answer.getId(),
                            answer.getText(),
                            idsSelectedAnswerOption.contains(answer.getId()),
                            answer.isCorrect(),
                            null,
                            null
                    ))
                    .toList();
        } else if (typeQuestion.equals(Type.OPEN_TEXT)) {
            List<String> answersTextCorrect = question
                    .getCorrectTextAnswers()
                    .stream()
                    .map(answer -> answer.trim().toLowerCase())
                    .toList();
            return List.of(new ResultResponseDto.UserAnswerResponseDto(
                    null,
                    null,
                    false,
                    answersTextCorrect.contains(userTextAnswer.trim().toLowerCase()),
                    userTextAnswer,
                    question.getCorrectTextAnswers()
            ));
        } else {
            log.error("Не известный тип вопроса");
            throw new IllegalArgumentException("Не известный тип вопроса");
        }
    }

    /**
     * Строит список DTO вопросов для передачи теста пользователю на прохождение.
     *
     * @param questions список вопросов.
     * @param idQuestionToAnswerOptions Map вариантов ответов по ID вопроса.
     * @return список DTO вопросов для прохождения теста.
     */
    public static List<TestToPassDto.QuestionDto> buildQuestionDtoTestToPass(
            List<Question> questions,
            Map<Long, List<AnswerOption>> idQuestionToAnswerOptions
    ) {
        return questions
                .stream()
                .map(question -> {
                    final Long idQuestion = question.getId();
                    List<TestToPassDto.AnswerOptionDto> answerOptionsDto = idQuestionToAnswerOptions
                            .getOrDefault(idQuestion, new ArrayList<>())
                            .stream()
                            .map(answer ->
                                    new TestToPassDto.AnswerOptionDto(
                                            answer.getId(),
                                            answer.getText()
                                    ))
                            .toList();

                    return new TestToPassDto.QuestionDto(
                            idQuestion,
                            question.getText(),
                            question.getType(),
                            answerOptionsDto
                    );
                })
                .toList();
    }

    /**
     * Строит список DTO вопросов для обновления содержимого теста.
     *
     * @param questions список вопросов.
     * @param idQuestionToAnswerOptions Map вариантов ответов по ID вопроса.
     * @return список DTO вопросов для обновления теста.
     */
    public static List<TestUpdateTestContentDto.QuestionDto> buildQuestionsDtoTestUpdate(
            List<Question> questions,
            Map<Long, List<AnswerOption>> idQuestionToAnswerOptions
    ) {
        return questions.stream()
                .map(question -> {
                    List<AnswerOption> currentOptions = idQuestionToAnswerOptions.getOrDefault(question.getId(), Collections.emptyList());

                    List<TestUpdateTestContentDto.AnswerOptionDto> answerOptionsDto = currentOptions.stream()
                            .map(answerOption -> new TestUpdateTestContentDto.AnswerOptionDto(
                                    answerOption.getId(),
                                    answerOption.getText(),
                                    answerOption.isCorrect(),
                                    answerOption.getExplanation()
                            ))
                            .toList();

                    return new TestUpdateTestContentDto.QuestionDto(
                            question.getId(),
                            question.getText(),
                            question.getType(),
                            question.getPoint(),
                            question.getCorrectTextAnswers(),
                            answerOptionsDto
                    );
                })
                .toList();
    }
}
