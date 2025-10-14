package ru.viktorgezz.NauJava.service.intrf;

import ru.viktorgezz.NauJava.model.TestModel;

import java.util.List;

/**
 * Сервисный интерфейс для управления сущностями тестов.
 *
 * <p>Отвечает за бизнес-операции над {@link ru.viktorgezz.NauJava.model.TestModel}:
 * создание, поиск, получение списка, удаление и частичное обновление
 * (названия и описания).</p>
 */
public interface TestService {

    void createTest(TestModel test);

    TestModel findById(Long id);

    List<TestModel> findAll();

    void deleteById(Long id);

    void updateTitleAndDescription(Long id, String title, String description);
}
