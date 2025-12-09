package ru.viktorgezz.NauJava.domain.result.service.intrf;

import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.result.dto.ResultRequestDto;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис для управления результатами тестов (Result).
 */
public interface ResultCommandService {

    /**
     * Инициирует создание результата теста и возвращает его ID.
     *
     * @param resultRequestDto DTO с данными результата теста.
     * @return ID созданного результата.
     */
    Long initiateCompilationResult(ResultRequestDto resultRequestDto);

    /**
     * Асинхронно компилирует результат теста: обрабатывает ответы, вычисляет баллы и оценку.
     *
     * @param idQuestionToUserAnswer карта ответов пользователя по ID вопросов.
     * @param idResult ID результата для компиляции.
     * @return CompletableFuture с скомпилированным результатом.
     */
    CompletableFuture<Result> compilateResultAsync(Map<Long, ResultRequestDto.UserAnswerRequestDto> idQuestionToUserAnswer, Long idResult);

    /**
     * Удаляет результат теста по его ID, включая все связанные ответы пользователя.
     * Операция выполняется транзакционно.
     *
     * @param idResult ID результата теста для удаления.
     * @throws jakarta.persistence.EntityNotFoundException если результат с таким ID не найден.
     * @throws org.springframework.dao.DataAccessException в случае других ошибок доступа к данным.
     */
    void deleteResult(Long idResult);
}
