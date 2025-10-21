package ru.viktorgezz.NauJava.result.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.viktorgezz.NauJava.result.Grade;
import ru.viktorgezz.NauJava.result.Result;

import java.math.BigDecimal;
import java.util.List;

public interface ResultRepo extends CrudRepository<Result, Long> {

    /**
     * Находит все пройденные тесты пользователя с определенной оценкой
     * @param grade Оценка за текст
     * @param userId id пользователя
     * @return List<Result>
     */
    List<Result> findAllByGradeAndParticipantId(Grade grade, Long userId);

    /**
     * Возвращает все пройденные тесты с балами ниже определенного числа
     * @param maxScore максимально количество балов, не включительно
     * @return List<Result>
     */
    @Query("SELECT r FROM Result r WHERE r.score < :maxScore")
    List<Result> findWithScoreLessThan(@Param("maxScore") BigDecimal maxScore);


}
