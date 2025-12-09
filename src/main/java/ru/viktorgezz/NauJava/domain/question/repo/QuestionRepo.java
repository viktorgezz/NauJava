package ru.viktorgezz.NauJava.domain.question.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.viktorgezz.NauJava.domain.question.Question;

import java.util.List;

/**
 * Репозиторий для доступа к сущностям {@link Question}.
 */
@RepositoryRestResource(path = "questions")
public interface QuestionRepo extends CrudRepository<Question, Long> {

    /**
     * Найти вопросы по id
     *
     * @param idTest idTest теста
     * @return список вопросов теста
     */
    List<Question> findByTestId(Long idTest);

}
