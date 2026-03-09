package ru.viktorgezz.testing_system.domain.answer_option.repo;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.viktorgezz.testing_system.domain.answer_option.AnswerOption;

import java.util.List;

/**
 * Репозиторий для доступа к сущностям {@link AnswerOption}.
 */
public interface AnswerOptionRepo extends CrudRepository<AnswerOption, Long> {

    @Query("""
            SELECT DISTINCT answerOption FROM AnswerOption answerOption
            LEFT JOIN FETCH answerOption.question question
            WHERE question.id IN :idsQuestion
            """)
    List<AnswerOption> findAllByIdsQuestionWithQuestion(@Param("idsQuestion") Iterable<Long> idsQuestion);
}
