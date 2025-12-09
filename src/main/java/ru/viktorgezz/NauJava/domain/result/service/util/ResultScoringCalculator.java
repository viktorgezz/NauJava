package ru.viktorgezz.NauJava.domain.result.service.util;

import jakarta.persistence.EntityNotFoundException;
import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.question.Type;
import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.result.dto.ResultRequestDto;
import ru.viktorgezz.NauJava.domain.user_answer.UserAnswer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Утилитный класс для расчёта баллов и оценок за прохождение теста.
 * <p>
 * Предоставляет методы для:
 * <ul>
 *     <li>Извлечения идентификаторов вопросов и вариантов ответов</li>
 *     <li>Расчёта баллов за вопросы с выбором ответа (SINGLE_CHOICE, MULTIPLE_CHOICE)</li>
 *     <li>Расчёта баллов за вопросы с открытым текстовым ответом (OPEN_TEXT)</li>
 *     <li>Подсчёта ошибок и частичного начисления баллов</li>
 * </ul>
 * </p>
 *
 * @see ru.viktorgezz.NauJava.domain.result.service.impl.ResultCommandServiceImpl
 */
public class ResultScoringCalculator {

    /**
     * Рассчитывает общий балл за тест и собирает ответы пользователя.
     */
    public static BigDecimal calculateTotalScoreAndCollectUserAnswers(
            List<Question> questions,
            Map<Long, ResultRequestDto.UserAnswerRequestDto> idQuestionToUserAnswer,
            Map<Long, List<AnswerOption>> idQuestionToAnswerOptions,
            Result resultCompilated,
            List<UserAnswer> userAnswersProcessed
    ) {
        return questions.stream()
                .map(question -> processQuestion(
                        question,
                        idQuestionToUserAnswer,
                        idQuestionToAnswerOptions,
                        resultCompilated,
                        userAnswersProcessed
                ))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Обрабатывает один вопрос и возвращает набранный балл.
     */
    private static BigDecimal processQuestion(
            Question question,
            Map<Long, ResultRequestDto.UserAnswerRequestDto> idQuestionToUserAnswer,
            Map<Long, List<AnswerOption>> idQuestionToAnswerOptions,
            Result resultCompilated,
            List<UserAnswer> userAnswersProcessed
    ) {
        Type typeQuestion = question.getType();

        if (isChoiceQuestion(typeQuestion)) {
            return processChoiceQuestion(
                    question,
                    idQuestionToUserAnswer,
                    idQuestionToAnswerOptions,
                    resultCompilated,
                    userAnswersProcessed
            );
        } else if (typeQuestion.equals(Type.OPEN_TEXT)) {
            return processOpenTextQuestion(question, idQuestionToUserAnswer, resultCompilated, userAnswersProcessed);
        }

        return BigDecimal.ZERO;
    }

    /**
     * Обрабатывает вопрос с выбором ответа (одиночный или множественный).
     */
    private static BigDecimal processChoiceQuestion(
            Question question,
            Map<Long, ResultRequestDto.UserAnswerRequestDto> idQuestionToUserAnswer,
            Map<Long, List<AnswerOption>> idQuestionToAnswerOptions,
            Result resultCompilated,
            List<UserAnswer> userAnswersProcessed
    ) {
        Set<Long> idsSelectedAnswerOptions = extractSelectedAnswerOptionIds(question.getId(), idQuestionToUserAnswer);
        List<AnswerOption> answerOptions = idQuestionToAnswerOptions.get(question.getId());
        Set<Long> idsCorrectOption = extractCorrectOptionIds(answerOptions);

        BigDecimal scoreCurr = calculateScoreForChoiceQuestion(
                question.getPoint(),
                question.isAllowMistakes(),
                idsCorrectOption,
                idsSelectedAnswerOptions
        );

        List<UserAnswer> userAnswersForQuestion = buildUserAnswersForSelectedOptions(
                idsSelectedAnswerOptions,
                idsCorrectOption,
                answerOptions,
                resultCompilated,
                question
        );
        userAnswersProcessed.addAll(userAnswersForQuestion);

        return scoreCurr;
    }

    /**
     * Извлекает идентификаторы выбранных пользователем вариантов ответа.
     */
    private static Set<Long> extractSelectedAnswerOptionIds(
            Long idQuestion,
            Map<Long, ResultRequestDto.UserAnswerRequestDto> idQuestionToUserAnswer
    ) {
        return new HashSet<>(
                idQuestionToUserAnswer
                        .getOrDefault(idQuestion, createEmptyUserAnswerDto())
                        .idsSelectedAnswerOption()
        );
    }

    /**
     * Извлекает идентификаторы правильных вариантов ответа.
     */
    private static Set<Long> extractCorrectOptionIds(List<AnswerOption> answerOptions) {
        return answerOptions.stream()
                .filter(AnswerOption::isCorrect)
                .map(AnswerOption::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Рассчитывает балл за вопрос с выбором ответа.
     */
    private static BigDecimal calculateScoreForChoiceQuestion(
            BigDecimal point,
            boolean isAllowMistakes,
            Set<Long> idsCorrectOption,
            Set<Long> idsSelectedAnswerOptions
    ) {
        BigDecimal countErrorsDecimal = BigDecimal.valueOf(countErrors(idsCorrectOption, idsSelectedAnswerOptions));

        if (countErrorsDecimal.compareTo(BigDecimal.ZERO) == 0) {
            return point;
        } else if (isAllowMistakes && !idsSelectedAnswerOptions.isEmpty()) {
            return calculatePartialScore(point, countErrorsDecimal, idsCorrectOption.size());
        }

        return BigDecimal.ZERO;
    }

    /**
     * Рассчитывает частичный балл с учётом допустимых ошибок.
     */
    private static BigDecimal calculatePartialScore(BigDecimal point, BigDecimal countErrorsDecimal, int totalOptionsCount) {
        BigDecimal totalOptions = BigDecimal.valueOf(totalOptionsCount);
        return point
                .multiply(BigDecimal.ONE.subtract(
                        countErrorsDecimal.divide(totalOptions, 4, RoundingMode.HALF_UP)
                ))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Создаёт список ответов пользователя для выбранных вариантов.
     */
    private static List<UserAnswer> buildUserAnswersForSelectedOptions(
            Set<Long> idsSelectedAnswerOptions,
            Set<Long> idsCorrectOption,
            List<AnswerOption> answerOptions,
            Result resultCompilated,
            Question question
    ) {
        return idsSelectedAnswerOptions.stream()
                .map(idSelectedOption -> new UserAnswer(
                        idsCorrectOption.contains(idSelectedOption),
                        resultCompilated,
                        question,
                        findAnswerOptionById(answerOptions, idSelectedOption)
                ))
                .toList();
    }

    /**
     * Находит вариант ответа по идентификатору.
     */
    private static AnswerOption findAnswerOptionById(List<AnswerOption> answerOptions, Long idAnswerOption) {
        return answerOptions.stream()
                .filter(answerOption -> answerOption.getId().equals(idAnswerOption))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("AnswerOption not found: " + idAnswerOption));
    }

    /**
     * Обрабатывает вопрос с открытым текстовым ответом.
     */
    private static BigDecimal processOpenTextQuestion(
            Question question,
            Map<Long, ResultRequestDto.UserAnswerRequestDto> idQuestionToUserAnswer,
            Result resultCompilated,
            List<UserAnswer> userAnswersProcessed
    ) {
        ResultRequestDto.UserAnswerRequestDto userAnswerDto = idQuestionToUserAnswer
                .getOrDefault(question.getId(), createEmptyUserAnswerDto());
        String textAnswer = userAnswerDto.textAnswerWritten().toLowerCase().trim();

        boolean isCorrect = question
                .getCorrectTextAnswers()
                .stream()
                .map(answerString -> answerString.trim().toLowerCase())
                .toList()
                .contains(textAnswer);
        userAnswersProcessed.add(new UserAnswer(textAnswer, isCorrect, resultCompilated, question));

        return isCorrect ? question.getPoint() : BigDecimal.ZERO;
    }

    /**
     * Создаёт пустой DTO ответа пользователя.
     */
    private static ResultRequestDto.UserAnswerRequestDto createEmptyUserAnswerDto() {
        return new ResultRequestDto.UserAnswerRequestDto("", List.of());
    }

    /**
     * Подсчитывает количество ошибок при сравнении правильных ответов с ответами пользователя.
     * Ошибка = элемент, который есть только в одном из списков.
     *
     * @param correctSet список правильных ответов
     * @param userSet    список ответов пользователя
     * @return количество ошибок
     */
    private static long countErrors(Set<Long> correctSet, Set<Long> userSet) {
        long missingCount = correctSet.stream().filter(id -> !userSet.contains(id)).count();
        long extraCount = userSet.stream().filter(id -> !correctSet.contains(id)).count();
        return missingCount + extraCount;
    }

    /**
     * Проверяет, является ли тип вопроса вопросом с выбором.
     */
    private static boolean isChoiceQuestion(Type typeQuestion) {
        return typeQuestion.equals(Type.SINGLE_CHOICE) || typeQuestion.equals(Type.MULTIPLE_CHOICE);
    }

}
