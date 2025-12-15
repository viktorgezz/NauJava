package ru.viktorgezz.NauJava.domain.result.repo;

import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.viktorgezz.NauJava.domain.result.Result;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Репозиторий для доступа к сущностям {@link Result}.
 */
@RepositoryRestResource(path = "results")
public interface ResultRepo extends CrudRepository<Result, Long> {

    @Override
    @NonNull
    List<Result> findAll();

    @Query("""
            SELECT result FROM Result result
            LEFT JOIN FETCH result.participant
            LEFT JOIN FETCH result.test
            """
    )
    List<Result> findAllWithParticipantUsernameAndTitleTest();

    @Query("""
            SELECT result FROM Result result
            LEFT JOIN FETCH result.test test
            LEFT JOIN FETCH test.questions
            WHERE result.id = :id
            """
    )
    Optional<Result> findByIdWithTestAndQuestions(@Param("id") Long id);

    /**
     * Получить результаты по списку ID с тестом.
     *
     * @param ids список ID результатов
     * @return результаты с подгруженным тестом
     */
    @Query("""
            SELECT result FROM Result result
            LEFT JOIN FETCH result.test test
            WHERE result.id IN :ids
            """)
    List<Result> findAllWithTestByIds(@Param("ids") List<Long> ids);

    @Query("""
            SELECT result FROM Result result
            LEFT JOIN result.test test
            WHERE result.participant.id = :idUser AND test.id = :idTest
            ORDER BY result.completedAt DESC
            """)
    Stream<Result> findResultLastAttempts(
            @Param("idTest") Long idTest,
            @Param("idUser") Long idUser
    );
}
