package ru.viktorgezz.NauJava.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.viktorgezz.NauJava.model.TestModel;
import ru.viktorgezz.NauJava.service.intrf.TestService;
import ru.viktorgezz.NauJava.util.TestArrayList;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Интеграционные тесты для TestServiceImpl")
class TestServiceImplTestModel {

    @Autowired
    private TestService testService;

    @Autowired
    private TestArrayList testArrayList;

    private TestModel testModel;
    private List<String> questions;

    @BeforeEach
    void setUp() {
        testArrayList.clear();

        questions = Arrays.asList(
                "Что такое Java?",
                "Что такое Spring?",
                "Что такое микросервис?"
        );

        testModel = new TestModel(
                "Основы Java",
                "Тест по основам Java",
                questions
        );
    }

    @Test
    @DisplayName("Должен успешно создавать тест")
    void testCreateTest() {
        testService.createTest(testModel);

        assertNotNull(testModel.getId());
        assertEquals("Основы Java", testModel.getTitle());
        assertEquals("Тест по основам Java", testModel.getDescription());
        assertEquals(1, testArrayList.size());
    }

    @Test
    @DisplayName("Должен успешно находить тест по id")
    void testFindByIdSuccess() {
        testService.createTest(testModel);
        Long testId = testModel.getId();

        TestModel result = testService.findById(testId);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals("Основы Java", result.getTitle());
        assertEquals("Тест по основам Java", result.getDescription());
        assertEquals(questions, result.getQuestions());
    }

    @Test
    @DisplayName("Должен выбрасывать исключение, если тест по id не найден")
    void testFindByIdNotFound() {
        Long testId = 999L;

        assertThrows(Exception.class, () -> testService.findById(testId));
    }

    @Test
    @DisplayName("Должен возвращать все тесты")
    void testFindAll() {
        TestModel test2 = new TestModel(
                "Продвинутый Spring Boot",
                "Продвинутые концепции Spring Boot",
                Arrays.asList("Внедрение зависимостей", "Жизненный цикл бина")
        );

        testService.createTest(testModel);
        testService.createTest(test2);

        List<TestModel> result = testService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Основы Java", result.get(0).getTitle());
        assertEquals("Продвинутый Spring Boot", result.get(1).getTitle());
    }

    @Test
    @DisplayName("Должен возвращать пустой список, если тестов нет")
    void testFindAllEmpty() {
        List<TestModel> result = testService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Должен успешно удалять тест по id")
    void testDeleteById() {
        testService.createTest(testModel);
        Long testId = testModel.getId();
        assertEquals(1, testArrayList.size());

        testService.deleteById(testId);

        assertEquals(0, testArrayList.size());
        assertThrows(Exception.class, () -> testService.findById(testId));
    }

    @Test
    @DisplayName("Должен успешно обновлять название и описание теста")
    void testUpdateTitleAndDescription() {
        testService.createTest(testModel);
        Long testId = testModel.getId();
        String newTitle = "Обновленные основы Java";
        String newDescription = "Обновленное описание";

        testService.updateTitleAndDescription(testId, newTitle, newDescription);

        TestModel result = testService.findById(testId);
        assertEquals(newTitle, result.getTitle());
        assertEquals(newDescription, result.getDescription());
        assertEquals(questions, result.getQuestions());
    }

    @Test
    @DisplayName("Должен выбрасывать исключение при обновлении несуществующего теста")
    void testUpdateNonExistentTest() {
        Long testId = 999L;

        assertThrows(Exception.class,
                () -> testService.updateTitleAndDescription(testId, "Новое название", "Новое описание"));
    }

    @Test
    @DisplayName("Должен сохранять вопросы при обновлении названия и описания")
    void testUpdatePreservesQuestions() {
        testService.createTest(testModel);
        Long testId = testModel.getId();
        String newTitle = "Обновленное название";
        String newDescription = "Обновленное описание";

        testService.updateTitleAndDescription(testId, newTitle, newDescription);

        TestModel result = testService.findById(testId);
        assertEquals(questions, result.getQuestions());
        assertEquals(newTitle, result.getTitle());
        assertEquals(newDescription, result.getDescription());
    }

    @Test
    @DisplayName("Должен обрабатывать null в названии при обновлении")
    void testUpdateWithNullTitle() {
        testService.createTest(testModel);
        Long testId = testModel.getId();

        testService.updateTitleAndDescription(testId, null, "Новое описание");

        TestModel result = testService.findById(testId);
        assertNull(result.getTitle());
        assertEquals("Новое описание", result.getDescription());
    }

    @Test
    @DisplayName("Должен иметь корректный размер списка после нескольких операций")
    void testMultipleOperations() {
        TestModel test2 = new TestModel(
                "Spring Boot",
                "Основы Spring Boot",
                Arrays.asList("Контроллеры", "Сервисы")
        );
        TestModel test3 = new TestModel(
                "Микросервисы",
                "Паттерны микросервисов",
                Arrays.asList("Circuit Breaker", "Service Discovery")
        );

        testService.createTest(testModel);
        testService.createTest(test2);
        testService.createTest(test3);
        assertEquals(3, testService.findAll().size());

        testService.deleteById(test2.getId());
        assertEquals(2, testService.findAll().size());

        List<TestModel> result = testService.findAll();
        assertFalse(result.stream().anyMatch(t -> t.getId().equals(test2.getId())));
    }
}