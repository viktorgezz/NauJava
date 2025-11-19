package ru.viktorgezz.NauJava.domain.result.repo;

import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.viktorgezz.NauJava.domain.result.Grade;
import ru.viktorgezz.NauJava.domain.result.Result;

import java.math.BigDecimal;
import java.util.List;

/**
 * Репозиторий для доступа к сущностям {@link Result}.
 */
@RepositoryRestResource(path = "results")
public interface ResultRepo extends CrudRepository<Result, Long> {

    /**
     * Находит все пройденные тесты пользователя с определенной оценкой
     *
     * @param grade  Оценка за текст
     * @param idUser id пользователя
     * @return List<Result>
     */
    List<Result> findAllByGradeAndParticipantId(Grade grade, Long idUser);

    /**
     * Возвращает все пройденные тесты с балами ниже определенного числа
     *
     * @param maxScore максимально количество балов, не включительно
     * @return List<Result>
     */
    @Query("SELECT r FROM Result r WHERE r.score < :maxScore")
    List<Result> findWithScoreLessThan(@Param("maxScore") BigDecimal maxScore);

    @Override
    @NonNull
    List<Result> findAll();

    @Query("""
            SELECT r FROM Result r
            LEFT JOIN FETCH r.participant
            LEFT JOIN FETCH r.test
            """
    )
    List<Result> findAllWithParticipantUsernameAndTitleTest();

}
