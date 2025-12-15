package ru.viktorgezz.NauJava.domain.topic.service;

import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.topic.Topic;

import java.util.List;
import java.util.Set;

/**
 * Контракт сервиса для работы с темами тестов.
 */
public interface TopicService {

    /**
     * Сохраняет темы и связывает их с тестом.
     *
     * @param topics  множество тем для сохранения и связи.
     * @param testNew тест для связи с темами.
     */
    void saveAndLinkAll(Set<Topic> topics, TestModel testNew);

    /**
     * Находит существующие темы по названиям или создает новые.
     *
     * @param titlesTopic список названий тем.
     * @return список найденных или созданных тем.
     */
    List<Topic> findOrCreateTopics(List<String> titlesTopic);

    /**
     * Сохраняет коллекцию тем в БД.
     *
     * @param topics коллекция тем для сохранения.
     */
    void saveAll(Iterable<Topic> topics);

}
