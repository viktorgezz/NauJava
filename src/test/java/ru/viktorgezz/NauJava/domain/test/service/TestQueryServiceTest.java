package ru.viktorgezz.NauJava.domain.test.service;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationPostgresTest;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.repo.TestRepo;
import ru.viktorgezz.NauJava.domain.test.service.intrf.TestQueryService;
import ru.viktorgezz.NauJava.domain.topic.Topic;
import ru.viktorgezz.NauJava.domain.topic.TopicRepo;
import ru.viktorgezz.NauJava.domain.topic.TopicService;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;
import ru.viktorgezz.NauJava.util.CreationModel;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.viktorgezz.NauJava.util.CreationModel.*;

@DisplayName("TestQueryService Integration Tests")
public class TestQueryServiceTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private TestQueryService testQueryService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private TestRepo testRepo;
    @Autowired
    private TopicRepo topicRepo;

    private TestModel testModel1;
    private User author;
    private Topic topic1;
    private Topic topicCommon;

    @BeforeEach
    void setup() {
        author = CreationModel.createRandomUser();
        userRepo.save(author);

        testModel1 = createTest("The test 1", "description 1", Status.PUBLIC, author);
        TestModel testModel2 = createTest("The test 2", "description 2", Status.PUBLIC, author);

        testRepo.saveAll(List.of(testModel1, testModel2));

        topic1 = createTopicRandom();
        Topic topic2 = createTopicRandom();
        topicCommon = createTopicRandom();

        topicRepo.saveAll(List.of(topic1, topic2, topicCommon));

        topicService.saveAndLinkAll(Set.of(topic1, topicCommon), testModel1);
        topicService.saveAndLinkAll(Set.of(topic2, topicCommon), testModel2);
    }

    @AfterEach
    void tearDown() {
        userRepo.deleteAll();
        testRepo.deleteAll();
        topicRepo.deleteAll();
    }

    /**
     * <p>Тестирование получения всех созданных тестов.
     * Проверяется, что метод возвращает все объекты, сохраненные в БД во время {@code setup()}.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Среда настроена методом {@code setup()}, в БД два теста.</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Вызов метода {@code testQueryService.findAll()}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что возвращенный список содержит ровно 2 элемента.</li>
     * <li>Проверить, что заголовки тестов в списке соответствуют ожидаемым ("The test 1", "The test 2").</li>
     * </ul>
     * </ol></b>
     */
    @Test
    @DisplayName("Поиск всех Тестов")
    void shouldFindAllTests() {
        List<TestModel> tests = testQueryService.findAll();
        assertThat(tests)
                .hasSize(2)
                .extracting(TestModel::getTitle)
                .contains("The test 1", "The test 2");
    }

    /**
     * <p>Тестирование поиска теста по точному названию.
     * Проверяется, что возвращается только один тест с искомым названием.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Определено целевое название теста ("The test 1").</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Вызов метода {@code testQueryService.findAllByTitle(testTitle)}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что возвращенный список содержит ровно 1 элемент.</li>
     * <li>Проверить, что название и ID найденного теста соответствуют ожидаемому ({@code testModel1}).</li>
     * </ul>
     * </ol></b>
     */
    @Test
    @DisplayName("Поиск теста по названию")
    void shouldFindAllTestsByTitle() {
        String testTitle = "The test 1";

        List<TestModel> tests = testQueryService.findAllByTitle(testTitle);
        assertThat(tests).hasSize(1);
        assertThat(tests.getFirst())
                .satisfies(t -> {
                    assertThat(t.getTitle()).isEqualTo(testTitle);
                    assertThat(t.getId()).isEqualTo(testModel1.getId());
                });
    }

    /**
     * <p>Тестирование поиска тестов по несуществующему названию.
     * Проверяется, что при передаче названия, не соответствующего ни одному тесту, возвращается пустой список.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Определено несуществующее название теста (пустая строка).</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Вызов метода {@code testQueryService.findAllByTitle(testTitle)}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что возвращенный список является пустым ({@code isEmpty}).</li>
     * </ul>
     * </ol></b>
     */
    @Test
    @DisplayName("Поиск тестов по несуществующего названию")
    void shouldReturnEmptyList_WhenTitleUnknown() {
        String testTitle = "";

        List<TestModel> tests = testQueryService.findAllByTitle(testTitle);
        assertThat(tests).isEmpty();
    }

    /**
     * <p>Тестирование поиска тестов при наличии нескольких тестов с одинаковым названием.
     * Проверяется, что метод находит все тесты с заданным названием, даже если их несколько.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Создать и сохранить в БД два новых объекта {@code TestModel} ({@code testNew1}, {@code testNew2}) с одинаковым названием ("Duplicate Title"), но разными ID и описаниями.</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Вызов метода {@code testQueryService.findAllByTitle(testTitle)}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что возвращенный список содержит ровно 2 элемента.</li>
     * <li>Проверить, что названия обоих найденных тестов совпадают, а их ID и описания различаются.</li>
     * </ul>
     * </ol></b>
     */
    @Test
    @DisplayName("Поиск нескольких тестов с одинаковым названием")
    void shouldFindAllTestsWithSameTitleByTitle() {
        String testTitle = "Duplicate Title";
        TestModel testNew1 = createTest(testTitle, "desc 1", Status.PUBLIC, author);
        TestModel testNew2 = createTest(testTitle, "desc 2", Status.PUBLIC, author);
        testRepo.save(testNew1);
        testRepo.save(testNew2);

        List<TestModel> testsFound = testQueryService.findAllByTitle(testTitle);
        assertThat(testsFound).hasSize(2);

        TestModel testFound1 = testsFound.getFirst();
        TestModel testFound2 = testsFound.getLast();

        assertThat(testFound1.getTitle()).isEqualTo(testFound2.getTitle());
        assertThat(testFound1.getId()).isNotEqualTo(testFound2.getId());
        assertThat(testFound1.getDescription()).isNotEqualTo(testFound2.getDescription());
    }

    /**
     * <p>Тестирование поиска тестов по списку названий тем (топиков).
     * Проверяется корректность поиска по одной теме, по нескольким темам одновременно и по несуществующей теме.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Тест 1 связан с {@code topic1} и {@code topicCommon}.</li>
     * <li>Тест 2 связан с {@code topic2} и {@code topicCommon}.</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Вызвать метод {@code testQueryService.findTestsByTopicsTitle()} с различными списками: ['Topic 1'], ['Topic Common'], ['Topic 1', 'Topic Common'], ['Unknown Topic'].</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>По {@code topic1} найден 1 тест.</li>
     * <li>По {@code topicCommon} найдено 2 теста ("The test 1", "The test 2").</li>
     * <li>По комбинации {@code topic1} и {@code topicCommon} найдено 2 теста.</li>
     * <li>По запросу 'Unknown Topic' возвращен пустой список.</li>
     * </ul>
     * </ol></b>
     */
    @Test
    @DisplayName("Поиск тестов по списку названий тем")
    void shouldFindTestsByTopicTitles() {
        String topicTitle1 = topic1.getTitle();
        String topicTitle2 = topicCommon.getTitle();

        List<TestModel> testsFoundByTopicTitle1 = testQueryService.findTestsByTopicsTitle(List.of(topicTitle1));
        List<TestModel> testsFoundByTopicTitle2 = testQueryService.findTestsByTopicsTitle(List.of(topicTitle2));
        List<TestModel> testsFoundByTopicTitles = testQueryService.findTestsByTopicsTitle(List.of(topicTitle1, topicTitle2));
        List<TestModel> resEmpty = testQueryService.findTestsByTopicsTitle(List.of("Unknown Topic"));


        assertThat(testsFoundByTopicTitle1).hasSize(1)
                .extracting(TestModel::getTitle).contains("The test 1");
        assertThat(testsFoundByTopicTitle2).hasSize(2)
                .extracting(TestModel::getTitle)
                .contains("The test 1", "The test 2");
        assertThat(testsFoundByTopicTitles).hasSize(2);
        assertThat(resEmpty).isEmpty();
    }

    /**
     * <p>Тестирование получения всех тестов с принудительной подгрузкой (FETCH JOIN) связанных сущностей:
     * автора ({@code author}) и тем ({@code testTopics}).
     * Проверяется, что поля связанных объектов инициализированы, что предотвращает проблему N+1.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Тестовая среда инициализирована методом {@code setup()}.</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Вызов метода {@code testQueryService.findAllWithAuthorAndTopics()}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Удостовериться, что список содержит тесты с ожидаемыми заголовками.</li>
     * <li>Удостовериться, что у всех тестов загружено поле {@code author} с корректным именем пользователя.</li>
     * <li>Удостовериться, что для теста "The test 1" корректно загружены связанные темы (названия соответствуют ожидаемым, например "Topic Common").</li>
     * </ul>
     * </ol></b>
     */
    @Test
    @DisplayName("Поиск всех тестов с подгрузкой авторов и тем (FETCH JOIN)")
    void shouldFindAllTestsWithAuthorAndTopics() {
        List<TestModel> tests = testQueryService.findAllWithAuthorAndTopics();

        TestModel testFound = tests.stream()
                .filter(test -> test.getTitle().equals("The test 1"))
                .findFirst().orElseThrow();

        assertThat(tests)
                .extracting(TestModel::getTitle)
                .contains(testModel1.getTitle());
        assertThat(tests)
                .extracting(test -> test.getAuthor().getUsername())
                .contains(testModel1.getAuthor().getUsername());
        assertThat(testFound.getTestTopics())
                .extracting(testTopic -> testTopic.getTopic().getTitle())
                .contains(topic1.getTitle(), topicCommon.getTitle());
    }

}
