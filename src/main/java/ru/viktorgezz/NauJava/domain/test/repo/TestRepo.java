package ru.viktorgezz.NauJava.domain.test.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.viktorgezz.NauJava.domain.test.TestModel;

import java.util.List;

/**
 * Репозиторий для доступа к сущностям {@link TestModel}.
 */
@RepositoryRestResource(path = "tests")
public interface TestRepo extends CrudRepository<TestModel, Long> {

    /**
     * Поиск тестов по названию
     *
     * @param title название теста
     * @return List<TestModel>
     */
    List<TestModel> findAllByTitle(String title);

    /**
     * Поиск тестов по списку названий тем.
     *
     * @param topicTitles Список названий тем.
     * @return List<TestModel> Список уникальных тестов, связанных хотя бы с одной из указанных тем.
     */
    @Query("SELECT DISTINCT tt.test FROM TestTopic tt WHERE tt.topic.title IN :topicTitles")
    List<TestModel> findTestsByTopicTitles(@Param("topicTitles") List<String> topicTitles);

    /**
     * Получить все тесты со связанными автором и темами.
     *
     * @return List<TestModel> все тесты с подгруженными автором и темами
     */
    @Query("""
               SELECT DISTINCT t FROM TestModel t
               LEFT JOIN FETCH t.author
               LEFT JOIN FETCH t.testTopics tt
               LEFT JOIN FETCH tt.topic
            """
    )
    List<TestModel> findAllWithAuthorAndTopics();
}
