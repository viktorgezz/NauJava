package ru.viktorgezz.NauJava.domain.test.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.viktorgezz.NauJava.domain.test.TestModel;

/**
 * Репозиторий для доступа к сущностям {@link TestModel} с пагинацией и поиском.
 */
@RepositoryRestResource(exported = false)
public interface TestPagingAndSortingRepo extends PagingAndSortingRepository<TestModel, Long> {

    /**
     * Получить ID всех публичных тестов с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return страница ID тестов
     */
    @Query("""
            SELECT test.id FROM TestModel test
            WHERE test.status = ru.viktorgezz.NauJava.domain.test.Status.PUBLIC
            """)
    Page<Long> findAllTestIds(Pageable pageable);

    /**
     * Поиск ID публичных тестов по частичному совпадению названия (без учёта регистра).
     *
     * @param title    подстрока для поиска в названии
     * @param pageable параметры пагинации
     * @return страница ID тестов
     */
    @Query("""
            SELECT test.id FROM TestModel test
            WHERE test.status = ru.viktorgezz.NauJava.domain.test.Status.PUBLIC
            AND LOWER(test.title) LIKE LOWER(CONCAT('%', :title, '%'))
            """)
    Page<Long> findTestIdsByTitle(@Param("title") String title, Pageable pageable);

    /**
     * Получить ID всех тестов текущего пользователя с пагинацией.
     *
     * @param userId   ID пользователя
     * @param pageable параметры пагинации
     * @return страница ID тестов
     */
    @Query("""
            SELECT test.id FROM TestModel test
            WHERE test.author.id = :userId
            """)
    Page<Long> findAllTestIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Поиск ID тестов текущего пользователя по частичному совпадению названия (без учёта регистра).
     *
     * @param userId   ID пользователя
     * @param title    подстрока для поиска в названии
     * @param pageable параметры пагинации
     * @return страница ID тестов
     */
    @Query("""
            SELECT test.id FROM TestModel test
            WHERE test.author.id = :userId
            AND LOWER(test.title) LIKE LOWER(CONCAT('%', :title, '%'))
            """)
    Page<Long> findTestIdsByUserIdAndTitle(@Param("userId") Long userId, @Param("title") String title, Pageable pageable);

}
