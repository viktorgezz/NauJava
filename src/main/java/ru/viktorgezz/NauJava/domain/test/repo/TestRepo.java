package ru.viktorgezz.NauJava.domain.test.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.viktorgezz.NauJava.domain.test.TestModel;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для доступа к сущностям {@link TestModel}.
 */
@RepositoryRestResource(path = "tests")
public interface TestRepo extends CrudRepository<TestModel, Long> {
    
    @Query("""
            SELECT DISTINCT test FROM TestModel test
            LEFT JOIN FETCH test.author
            WHERE test.id = :id
            """)
    Optional<TestModel> findByIdWithAuthor(@Param("id") Long id);

    /**
     * Получить все публичные тесты со связанными автором и темами.
     *
     * @return List<TestModel> все публичные тесты с подгруженными автором и темами
     */
    @Query("""
            SELECT DISTINCT test FROM TestModel test
            LEFT JOIN FETCH test.author
            LEFT JOIN FETCH test.testTopics tt
            LEFT JOIN FETCH tt.topic
            WHERE test.status = ru.viktorgezz.NauJava.domain.test.Status.PUBLIC
            """
    )
    List<TestModel> findAllWithAuthorAndTopics();

    /**
     * Получить тесты по списку ID с автором и темами.
     *
     * @param ids список ID тестов
     * @return тесты с подгруженными автором и темами
     */
    @Query("""
            SELECT DISTINCT test FROM TestModel test
            LEFT JOIN FETCH test.author
            LEFT JOIN FETCH test.testTopics testTopic
            LEFT JOIN FETCH testTopic.topic
            WHERE test.id IN :ids
            """)
    List<TestModel> findAllWithAuthorAndTopicsByIds(@Param("ids") List<Long> ids);

    /**
     * Получить все тесты текущего пользователя со связанными автором и темами.
     *
     * @param userId ID пользователя
     * @return List<TestModel> все тесты пользователя с подгруженными автором и темами
     */
    @Query("""
            SELECT DISTINCT test FROM TestModel test
            LEFT JOIN FETCH test.author
            LEFT JOIN FETCH test.testTopics tt
            LEFT JOIN FETCH tt.topic
            WHERE test.author.id = :userId
            """
    )
    List<TestModel> findAllWithAuthorAndTopicsByUserId(@Param("userId") Long userId);

    /**
     * Получить тесты текущего пользователя по списку ID с автором и темами.
     *
     * @param userId ID пользователя
     * @param ids     список ID тестов
     * @return тесты пользователя с подгруженными автором и темами
     */
    @Query("""
            SELECT DISTINCT test FROM TestModel test
            LEFT JOIN FETCH test.author
            LEFT JOIN FETCH test.testTopics testTopic
            LEFT JOIN FETCH testTopic.topic
            WHERE test.author.id = :userId
            AND test.id IN :ids
            """)
    List<TestModel> findAllWithAuthorAndTopicsByUserIdAndIds(@Param("userId") Long userId, @Param("ids") List<Long> ids);

    /**
     * Загрузить тест с вопросами и вариантами ответов для обновления.
     *
     * @param idTest id теста
     * @return TestModel с подгруженными вопросами и вариантами ответов
     */
    @Query("""
            SELECT DISTINCT test FROM TestModel test
            LEFT JOIN FETCH test.questions questions
            LEFT JOIN FETCH test.author author
            WHERE test.id = :idTest
            """)
    Optional<TestModel> findForEditingContent(@Param("idTest") Long idTest);

    /**
     * Загрузить тест с автором и темами для редактирования метаданных.
     *
     * @param idTest ID теста
     * @return тест с подгруженными автором и темами
     */
    @Query("""
            SELECT DISTINCT test FROM TestModel test
            LEFT JOIN FETCH test.author author
            LEFT JOIN FETCH test.testTopics testTopics
            LEFT JOIN FETCH testTopics.topic topic
            WHERE test.id = :idTest
            """)
    Optional<TestModel> findForEditingMetadata(@Param("idTest") Long idTest);

}
