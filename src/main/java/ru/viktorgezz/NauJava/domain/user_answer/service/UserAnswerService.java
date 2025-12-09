package ru.viktorgezz.NauJava.domain.user_answer.service;


import ru.viktorgezz.NauJava.domain.user_answer.UserAnswer;

import java.util.List;

/**
 * Сервис для работы с ответами пользователей на вопросы тестов.
 */
public interface UserAnswerService {

    /**
     * Сохраняет коллекцию ответов пользователя в БД.
     *
     * @param userAnswers коллекция ответов пользователя для сохранения.
     * @return список сохраненных ответов пользователя.
     */
    List<UserAnswer> saveAll(Iterable<UserAnswer> userAnswers);

    /**
     * Находит все ответы пользователя по ID вопросов и ID результата с подгруженными вопросами и вариантами ответов.
     *
     * @param idsQuestion коллекция ID вопросов.
     * @param idResult ID результата теста.
     * @return список ответов пользователя с подгруженными данными.
     */
    List<UserAnswer> findAllByIdsQuestionAndResultIdWithQuestionAndAnswerOption(
            Iterable<Long> idsQuestion,
            Long idResult
    );
}
