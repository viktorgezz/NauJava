package ru.viktorgezz.NauJava.topic;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Репозиторий для доступа к сущностям {@link Topic}.
 */
@RepositoryRestResource(path = "topics")
public interface TopicRepo extends CrudRepository<Topic, Long> {
}
