package ru.viktorgezz.NauJava.test_topic;

import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface TestTopicRepo extends CrudRepository<TestTopic, Long> {

    Set<TestTopic> findByTestId(Long id);
}
