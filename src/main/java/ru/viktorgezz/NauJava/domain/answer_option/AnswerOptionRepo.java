package ru.viktorgezz.NauJava.domain.answer_option;


import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Репозиторий для доступа к сущностям {@link AnswerOption}.
 */
@RepositoryRestResource(path = "answer-options")
public interface AnswerOptionRepo extends CrudRepository<AnswerOption, Long> {
}
