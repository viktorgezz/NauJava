package ru.viktorgezz.NauJava.domain.user_answer;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Репозиторий для доступа к сущностям {@link UserAnswer}.
 */
@RepositoryRestResource(path = "user-answers")
public interface UserAnswerRepo extends CrudRepository<UserAnswer, Long> {
}
