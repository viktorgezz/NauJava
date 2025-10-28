package ru.viktorgezz.NauJava.topic;

import ru.viktorgezz.NauJava.test.TestModel;

import java.util.List;
import java.util.Set;

/**
 * Контракт сервиса для работы с темами тестов.
 */
public interface TopicService {

    List<Topic> findAll();

    Topic save(Topic topic);

    Set<Topic> findAllById(Set<Long> idsTopic);

    void saveAndLinkAll(Set<Topic> foundTopics, TestModel newTest);
}
