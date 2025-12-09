package ru.viktorgezz.NauJava.domain.answer_option.service.intrf;

import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;

import java.util.List;

/**
 * Контракт сервисного слоя для выборок вариантов ответа {@link AnswerOption}
 */
public interface AnswerOptionQueryService {

    /**
     * Находит все варианты ответов по ID вопросов с подгруженными вопросами.
     *
     * @param idsQuestion коллекция ID вопросов.
     * @return список вариантов ответов с подгруженными вопросами.
     */
    List<AnswerOption> findAllAnswerOptionByIdsQuestionWithQuestion(Iterable<Long> idsQuestion);
}
