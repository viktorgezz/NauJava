package ru.viktorgezz.NauJava.domain.many_to_many_entity.test_topic;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Set;

/**
 * Репозиторий для доступа к сущностям {@link TestTopic} (связи тест–тема).
 */
@RepositoryRestResource(path = "tests-topics")
public interface TestTopicRepo extends CrudRepository<TestTopic, Long> {

    Set<TestTopic> findByTestId(Long id);
}
