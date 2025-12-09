package ru.viktorgezz.NauJava.domain.answer_option.service.intrf;

import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;
import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.test.dto.TestUpdateTestContentDto;

/**
 * Командный сервис для вариантов ответа {@link AnswerOption}
 */
public interface AnswerOptionCommandService {

    /**
     * Создает новый вариант ответа и связывает его с вопросом без сохранения в БД.
     *
     * @param questionExisting существующий вопрос для связи.
     * @param answerOptionDto DTO с данными варианта ответа.
     * @return созданный вариант ответа.
     */
    AnswerOption createAndLinkAnswerOption(Question questionExisting, TestUpdateTestContentDto.AnswerOptionDto answerOptionDto);

    /**
     * Обновляет существующий вариант ответа без сохранения в БД.
     *
     * @param answerOptionExisting существующий вариант ответа для обновления.
     * @param answerOptionDto DTO с новыми данными варианта ответа.
     */
    void updateAnswerOptionWithoutSave(AnswerOption answerOptionExisting, TestUpdateTestContentDto.AnswerOptionDto answerOptionDto);

    /**
     * Сохраняет коллекцию вариантов ответов в БД.
     *
     * @param answerOptions коллекция вариантов ответов для сохранения.
     */
    void saveAll(Iterable<AnswerOption> answerOptions);
}
