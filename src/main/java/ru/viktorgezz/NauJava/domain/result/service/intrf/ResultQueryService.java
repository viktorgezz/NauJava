package ru.viktorgezz.NauJava.domain.result.service.intrf;

import ru.viktorgezz.NauJava.domain.result.Grade;
import ru.viktorgezz.NauJava.domain.result.Result;

import java.math.BigDecimal;
import java.util.List;


/**
 * Сервис для получения результатов тестов (Result).
 */
public interface ResultQueryService {

    /**
     * Находит все пройденные тесты пользователя с определенной оценкой
     * @param grade Оценка за текст
     * @param idUser id пользователя
     * @return List<Result>
     */
    List<Result> findAllByGradeAndParticipantId(Grade grade, Long idUser);

    /**
     * Возвращает все пройденные тесты с балами ниже определенного числа
     * @param maxScore максимально количество балов, не включительно
     * @return List<Result>
     */
    List<Result> findWithScoreLessThan(BigDecimal maxScore);

    List<Result> findAll();

    List<Result> findAllWithParticipantUsernameAndTitleTest();
}
