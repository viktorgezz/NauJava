package ru.viktorgezz.NauJava.domain.result.service.intrf;

/**
 * Сервис для управления результатами тестов (Result).
 */
public interface ResultCommandService {

    /**
     * Удаляет результат теста по его ID, включая все связанные ответы пользователя.
     * Операция выполняется транзакционно.
     *
     * @param resultId ID результата теста для удаления.
     * @throws jakarta.persistence.EntityNotFoundException если результат с таким ID не найден.
     * @throws org.springframework.dao.DataAccessException в случае других ошибок доступа к данным.
     */
    void deleteResult(Long resultId);
}