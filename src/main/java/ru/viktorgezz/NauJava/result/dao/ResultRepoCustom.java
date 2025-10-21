package ru.viktorgezz.NauJava.result.dao;

import ru.viktorgezz.NauJava.result.Grade;
import ru.viktorgezz.NauJava.result.Result;

import java.math.BigDecimal;
import java.util.List;

public interface ResultRepoCustom {

    /**
     * Находит все пройденные тесты пользователя с определенной оценкой
     * @param grade Оценка за текст
     * @param userId id пользователя
     * @return List<Result>
     */
    List<Result> findAllByGradeAndUserId(Grade grade, Long userId);

    /**
     * Возвращает все пройденные тесты с балами ниже определенного числа
     * @param maxScore максимально количество балов, не включительно
     * @return List<Result>
     */
    List<Result> findWithScoreLessThan(BigDecimal maxScore);
}
