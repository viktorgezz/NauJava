package ru.viktorgezz.NauJava.test.dao;

import ru.viktorgezz.NauJava.test.TestModel;

import java.util.List;

public interface TestRepoCustom {

    /**
     * Поиск тестов по названию
     * @param title название теста
     * @return List<TestModel>
     */
    List<TestModel> findByTitle(String title);

    /**
     * Поиск тестов по списку названий тем.
     * @param topicTitles Список названий тем.
     * @return List<TestModel> Список уникальных тестов.
     */
    List<TestModel> findTestsByTopicTitles(List<String> topicTitles);
}
