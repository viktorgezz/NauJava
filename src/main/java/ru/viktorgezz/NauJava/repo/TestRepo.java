package ru.viktorgezz.NauJava.repo;

import ru.viktorgezz.NauJava.model.TestModel;

/**
 * Репозиторий для работы с {@link ru.viktorgezz.NauJava.model.TestModel}.
 *
 * <p>Наследует базовые CRUD-операции от {@link CrudRepository}.</p>
 */
public interface TestRepo extends CrudRepository<TestModel, Long> {
}
