package ru.viktorgezz.NauJava.repo;

import java.util.List;
import java.util.Optional;

/**
 * Базовый интерфейс репозитория с CRUD-операциями.
 *
 * <p>Определяет минимальный контракт для работы с сущностями:
 * сохранение, поиск по идентификатору, получение всех записей,
 * обновление и удаление по идентификатору.</p>
 *
 * @param <T>  тип доменной сущности
 * @param <ID> тип идентификатора сущности
 */
public interface CrudRepository<T, ID>
{
    void save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    void update(T entity);

    void deleteById(ID id);
}
