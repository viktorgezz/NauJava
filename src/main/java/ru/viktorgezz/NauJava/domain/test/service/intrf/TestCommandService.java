package ru.viktorgezz.NauJava.domain.test.service.intrf;

import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.dto.TestMetadataRequestDto;
import ru.viktorgezz.NauJava.domain.test.dto.TestUpdateContentDto;

/**
 * Контракт сервиса для управления тестами {@link TestModel}.
 */
public interface TestCommandService {

    /**
     * Обновляет метаданные теста (название, описание, статус, темы).
     * Если ID теста указан, обновляет существующий тест, иначе создает новый.
     *
     * @param testDto DTO с метаданными теста.
     * @return ID обновленного или созданного теста.
     */
    Long updateTestMetadata(TestMetadataRequestDto testDto);

    /**
     * Обновляет содержимое теста (вопросы и варианты ответов).
     *
     * @param testDto DTO с содержимым теста для обновления.
     */
    void updateTestContent(TestUpdateContentDto testDto);

    void deleteById(Long id);
}
