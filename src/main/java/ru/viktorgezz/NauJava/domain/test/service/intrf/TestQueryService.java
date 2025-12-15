package ru.viktorgezz.NauJava.domain.test.service.intrf;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.dto.TestToPassDto;
import ru.viktorgezz.NauJava.domain.test.dto.TestUpdateContentDto;

/**
 * Контракт сервиса для чтения тестов {@link TestModel}.
 */
public interface TestQueryService {

    /**
     * Находит тест по ID.
     *
     * @param id ID теста.
     * @return найденный тест.
     * @throws jakarta.persistence.EntityNotFoundException если тест с таким ID не найден.
     */
    TestModel findById(Long id);

    /**
     * Получает тест для прохождения по ID и преобразует его в DTO.
     *
     * @param id ID теста.
     * @return DTO теста с вопросами и вариантами ответов для прохождения.
     * @throws jakarta.persistence.EntityNotFoundException если тест с таким ID не найден.
     */
    TestToPassDto findTestToPassById(Long id);

    /**
     * Получает тест по ID с полным содержимым для редактирования.
     *
     * @param id ID теста.
     * @return DTO теста с полным содержимым (вопросы и варианты ответов).
     * @throws jakarta.persistence.EntityNotFoundException если тест с таким ID не найден.
     */
    TestUpdateContentDto findByIdWithContent(Long id);

    /**
     * Получает все тесты с информацией об авторе и темах с пагинацией.
     *
     * @param pageable параметры пагинации.
     * @return страница тестов.
     */
    Page<TestModel> findAllWithAuthorAndTopics(Pageable pageable);

    /**
     * Ищет тесты по названию с пагинацией.
     *
     * @param title название теста для поиска.
     * @param pageable параметры пагинации.
     * @return страница найденных тестов.
     */
    Page<TestModel> findByTitle(String title, Pageable pageable);

}
