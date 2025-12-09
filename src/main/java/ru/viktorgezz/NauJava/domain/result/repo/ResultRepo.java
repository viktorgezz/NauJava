package ru.viktorgezz.NauJava.domain.result.repo;

import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.viktorgezz.NauJava.domain.result.Result;

import java.util.List;
import java.util.Optional;

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

}
