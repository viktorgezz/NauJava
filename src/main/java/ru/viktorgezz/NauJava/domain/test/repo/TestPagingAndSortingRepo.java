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
     * Получить ID всех тестов с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return страница ID тестов
     */
    @Query("""
            SELECT test.id FROM TestModel test
            """)
    Page<Long> findAllTestIds(Pageable pageable);

    /**
     * Поиск ID тестов по частичному совпадению названия (без учёта регистра).
     *
     * @param string   подстрока для поиска в названии
     * @param pageable параметры пагинации
     * @return страница ID тестов
     */
    @Query("""
            SELECT test.id FROM TestModel test
            WHERE LOWER(test.title) LIKE LOWER(CONCAT('%', :title, '%'))
            """)
    Page<Long> findTestIdsByTitle(@Param("title") String string, Pageable pageable);

}
