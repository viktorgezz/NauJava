package ru.viktorgezz.NauJava.domain.result.service.intrf;

import ru.viktorgezz.NauJava.domain.result.Result;
import ru.viktorgezz.NauJava.domain.result.dto.ResultResponseDto;

import java.util.List;


/**
 * Сервис для получения результатов тестов (Result).
 */
public interface ResultQueryService {

    /**
     * Получает результат теста по ID и преобразует его в DTO с полной информацией.
     *
     * @param id ID результата теста.
     * @return DTO результата теста с вопросами и ответами пользователя.
     */
    ResultResponseDto getTestResultDto(Long id);

    /**
     * Получает все результаты тестов с информацией об участнике и названии теста.
     *
     * @return список всех результатов тестов.
     */
    List<Result> findAllWithParticipantUsernameAndTitleTest();
}
