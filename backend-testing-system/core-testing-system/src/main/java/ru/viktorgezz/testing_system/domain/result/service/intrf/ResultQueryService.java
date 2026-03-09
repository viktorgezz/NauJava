package ru.viktorgezz.testing_system.domain.result.service.intrf;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.viktorgezz.testing_system.domain.result.Result;
import ru.viktorgezz.testing_system.domain.result.dto.ResultMetadataResponseDto;
import ru.viktorgezz.testing_system.domain.result.dto.ResultResponseDto;
import ru.viktorgezz.testing_system.domain.result.dto.ResultShortMetadataResponseDto;

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

    Page<ResultMetadataResponseDto> findUserResults(Pageable pageable);

    List<ResultShortMetadataResponseDto> findResultLastThreeAttempts(Long idTest);
}
