package ru.viktorgezz.testing_system.domain.user_answer.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.viktorgezz.testing_system.domain.user_answer.UserAnswer;

import java.util.List;

/**
 * Репозиторий для доступа к сущностям {@link UserAnswer}.
 */
public interface UserAnswerRepo extends CrudRepository<UserAnswer, Long> {

    @Query("""
            SELECT DISTINCT userAnswer From UserAnswer userAnswer
            LEFT JOIN FETCH userAnswer.question question
            LEFT JOIN FETCH userAnswer.answerOption answerOption
            WHERE question.id IN :idsQuestion
            AND userAnswer.result.id = :idResult
            """)
    List<UserAnswer> findAllByIdsQuestionAndResultIdWithQuestionAndAnswerOption(
            @Param("idsQuestion") Iterable<Long> idsQuestion,
            @Param("idResult") Long idResult
    );
}
