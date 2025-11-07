package ru.viktorgezz.NauJava.domain.test.service.intrf;

import ru.viktorgezz.NauJava.domain.test.TestModel;

import java.util.List;

/**
 * Контракт сервиса для чтения тестов {@link TestModel}.
 */
public interface TestQueryService {

    /**
     * Возвращение всех тестов с ленивыми инициализация
     * @return List<TestModel>
     */
    List<TestModel> findAll();

    /**
     * Поиск тестов по названию
     * @param title название теста
     * @return List<TestModel>
     */
    List<TestModel> findByTitle(String title);

    /**
     * Поиск тестов по списку названий тем.
     * @param topicTitles Список названий тем.
     * @return List<TestModel> Список уникальных тестов, связанных хотя бы с одной из указанных тем.
     */
    List<TestModel> findTestsByTopicTitles(List<String> topicTitles);

    /**
     * Получить все тесты со связанными автором и темами.
     * @return List<TestModel> все тесты с подгруженными автором и темами
     */
    List<TestModel> findAllWithAuthorAndTopics();

}
