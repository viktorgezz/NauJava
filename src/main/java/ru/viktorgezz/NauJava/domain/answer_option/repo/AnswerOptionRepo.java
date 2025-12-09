package ru.viktorgezz.NauJava.domain.answer_option.repo;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.viktorgezz.NauJava.domain.answer_option.AnswerOption;

import java.util.List;

/**
 * Репозиторий для доступа к сущностям {@link AnswerOption}.
 */
@RepositoryRestResource(path = "answer-options")
public interface AnswerOptionRepo extends CrudRepository<AnswerOption, Long> {

    @Query("""
            SELECT DISTINCT answerOption FROM AnswerOption answerOption
            LEFT JOIN FETCH answerOption.question question
            WHERE question.id IN :idsQuestion
            """)
    List<AnswerOption> findAllByIdsQuestionWithQuestion(@Param("idsQuestion") Iterable<Long> idsQuestion);
}
