package ru.viktorgezz.testing_system.domain.many_to_many_entity.test_topic;

import org.springframework.data.repository.CrudRepository;

/**
 * Репозиторий для доступа к сущностям {@link TestTopic} (связи тест–тема).
 */
public interface TestTopicRepo extends CrudRepository<TestTopic, Long> {
}
