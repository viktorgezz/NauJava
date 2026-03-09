package ru.viktorgezz.testing_system.domain.question.repo;

import org.springframework.data.repository.CrudRepository;
import ru.viktorgezz.testing_system.domain.question.Question;

import java.util.List;

/**
 * Репозиторий для доступа к сущностям {@link Question}.
 */
public interface QuestionRepo extends CrudRepository<Question, Long> {

    /**
     * Найти вопросы по id
     *
     * @param idTest idTest теста
     * @return список вопросов теста
     */
    List<Question> findByTestId(Long idTest);

}
