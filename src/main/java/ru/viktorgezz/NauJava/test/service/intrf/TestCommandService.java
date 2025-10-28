package ru.viktorgezz.NauJava.test.service.intrf;

import ru.viktorgezz.NauJava.test.TestModel;
import ru.viktorgezz.NauJava.test.dto.TestRequestDto;
import ru.viktorgezz.NauJava.test.dto.TestRequestThymeleafDto;

/**
 * Контракт сервиса для управления тестами {@link TestModel}.
 */
public interface TestCommandService {

    /**
     * Создает новый тест вместе с его вопросами и связями с темами.
     * Операция выполняется в одной транзакции.
     *
     * @param createRequest DTO с данными для создания теста.
     * @return Созданный и сохраненный объект TestModel.
     */
    TestModel createTest(TestRequestDto createRequest);

    TestModel createTestThymeleaf(TestRequestThymeleafDto testDto);

}
