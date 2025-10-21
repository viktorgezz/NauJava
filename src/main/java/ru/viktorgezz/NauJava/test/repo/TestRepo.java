package ru.viktorgezz.NauJava.test.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.viktorgezz.NauJava.test.TestModel;

import java.util.List;

public interface TestRepo extends CrudRepository<TestModel, Long> {

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
    @Query("SELECT DISTINCT tt.test FROM TestTopic tt WHERE tt.topic.title IN :topicTitles")
    List<TestModel> findTestsByTopicTitles(@Param("topicTitles") List<String> topicTitles);
}
