package ru.viktorgezz.NauJava.test.service.intrf;

import ru.viktorgezz.NauJava.test.TestModel;
import ru.viktorgezz.NauJava.test.dto.TestRequestDto;
import ru.viktorgezz.NauJava.test.dto.TestRequestThymeleafDto;
import ru.viktorgezz.NauJava.user.User;

/**
 * Контракт сервиса для управления тестами {@link TestModel}.
 */
public interface TestCommandService {

    /**
     * Создает новый тест вместе с его вопросами и связями с темами.
     *
     * @param createRequest DTO с данными для создания теста.
     * @param author Аутентифицированный пользователь, который является автором теста.
     * @return Созданный и сохраненный объект TestModel.
     */
    TestModel createTest(TestRequestDto createRequest, User author);

    /**
     * Создает новый тест с темами.
     *
     * @param testDto DTO с данными для создания теста c Thymeleaf.
     * @param author  Аутентифицированный пользователь, который является автором теста.
     * @return Созданный и сохраненный объект TestModel.
     */
    TestModel createTestThymeleaf(TestRequestThymeleafDto testDto, User author);

}
