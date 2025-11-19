package ru.viktorgezz.NauJava.domain.topic.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationPostgresTest;
import ru.viktorgezz.NauJava.domain.many_to_many_entity.test_topic.TestTopic;
import ru.viktorgezz.NauJava.domain.test.Status;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.repo.TestRepo;
import ru.viktorgezz.NauJava.domain.topic.Topic;
import ru.viktorgezz.NauJava.domain.topic.TopicRepo;
import ru.viktorgezz.NauJava.domain.topic.TopicService;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.viktorgezz.NauJava.util.CreationModel.*;

@DisplayName("TopicServiceTest Integration Tests")
public class TopicServiceTest extends AbstractIntegrationPostgresTest {

    @Autowired
    private TopicService topicService;

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private TopicRepo topicRepo;
    @Autowired
    private TestRepo testRepo;

    private Topic topic1;
    private Topic topic2;

    private TestModel test;


    @BeforeEach
    void setUp() {
        topic1 = createTopic("Topic 1");
        topic2 = createTopic("Topic 2");
        topicRepo.saveAll(List.of(topic1, topic2));

        User author = createRandomUser();
        userRepo.save(author);

        test = createTest("The test 1", "desc 1", Status.PRIVATE, author);
        testRepo.save(test);
    }

    @AfterEach
    void tearDown() {
        userRepo.deleteAll();
        topicRepo.deleteAll();
        testRepo.deleteAll();
    }

    /**
     * <p>Тестирование получения всех созданных тем.
     * Проверяется, что метод возвращает все объекты {@code Topic}, сохраненные в БД во время {@code setUp()}.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Среда настроена методом {@code setUp()}, в БД две темы ("Topic 1", "Topic 2").</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Вызов метода {@code topicService.findAll()}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что возвращенный список содержит ровно 2 элемента.</li>
     * <li>Проверить, что названия тем в списке соответствуют ожидаемым.</li>
     * </ul>
     * </ol></b>
     */
    @Test
    @DisplayName("Поиск всех тем")
    void shouldFindAll() {
        List<Topic> topics = topicService.findAll();

        assertThat(topics).hasSize(2);
        assertThat(topics).extracting(Topic::getTitle).containsOnly("Topic 1", "Topic 2");
    }

    /**
     * <p>Тестирование поиска тем по уникальному набору их ID.
     * Проверяется, что метод находит все темы, ID которых были переданы.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Формируется набор ID существующих тем ({@code topic1.getId()}, {@code topic2.getId()}).</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Вызов метода {@code topicService.findAllByIds(ids)}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что возвращенный набор содержит ровно 2 темы.</li>
     * <li>Проверить, что названия тем в наборе соответствуют ожидаемым.</li>
     * </ul>
     * </ol></b>
     */
    @Test
    @DisplayName("Поиск тем по их уникальному набору id")
    void shouldFindAllByIdsExist() {
        Set<Long> ids = Set.of(topic1.getId(), topic2.getId());
        Set<Topic> topics = topicService.findAllByIds(ids);

        assertThat(topics).hasSize(2);
        assertThat(topics).extracting(Topic::getTitle).containsOnly("Topic 1", "Topic 2");
    }

    /**
     * <p>Тестирование поиска тем при передаче пустого набора ID.
     * Проверяется, что в ответ возвращается пустой набор тем.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Формируется пустой набор ID.</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Вызов метода {@code topicService.findAllByIds(ids)}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что возвращенный набор является пустым.</li>
     * </ul>
     * </ol></b>
     */
    @Test
    @DisplayName("Поиск тем по их пустому набору id")
    void shouldReturnEmptySet_WhenIdsEmpty() {
        Set<Long> ids = Set.of();
        Set<Topic> topics = topicService.findAllByIds(ids);

        assertThat(topics).isEmpty();
        assertThat(topics).extracting(Topic::getTitle).doesNotContain("Topic 1", "Topic 2");
    }

    /**
     * <p>Тестирование поиска тем, когда часть переданных ID существует, а часть — нет.
     * Проверяется, что возвращаются только существующие темы, а несуществующие ID игнорируются.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Формируется набор ID, содержащий один существующий ID ({@code topic1.getId()}) и один несуществующий ({@code 999999L}).</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Вызов метода {@code topicService.findAllByIds(requestedIds)}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что возвращенный набор содержит ровно 1 элемент.</li>
     * <li>Проверить, что ID найденной темы соответствует существующему ID, а несуществующий ID отсутствует.</li>
     * </ul>
     * </ol></b>
     */
    @Test
    @DisplayName("Поиск тем по частичному совпадению")
    void findAllByIds_WhenSomeIdsDoNotExist_ShouldReturnOnlyExisting() {
        Long existingId = topic1.getId();
        Long nonExistentId = 999999L;
        Set<Long> requestedIds = Set.of(existingId, nonExistentId);

        Set<Topic> foundTopics = topicService.findAllByIds(requestedIds);

        assertThat(foundTopics)
                .hasSize(1)
                .extracting(Topic::getId)
                .containsOnly(existingId)
                .doesNotContain(nonExistentId);
    }

    /**
     * <p>Тестирование ключевой функциональности: сохранение тем и создание связей многие-ко-многим
     * с моделью теста ({@code TestModel}) с использованием метода {@code saveAndLinkAll()}.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>В БД существуют темы {@code topic1}, {@code topic2} и тест {@code test}.</li>
     * <li>Формируется набор тем для связывания.</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Вызов метода {@code topicService.saveAndLinkAll(topics, test)}.</li>
     * <li>Получение обновленного теста с подгрузкой всех связей.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что в репозитории тем найдены только темы {@code topic1} и {@code topic2}, связанные с ID данного теста.</li>
     * <li>Проверить, что связанные сущности {@code TestTopic} корректно указывают на исходный объект теста.</li>
     * </ul>
     * </ol></b>
     */
    @Test
    @DisplayName("Сохранение и связывание тем с тестом")
    void shouldSaveAndLinkTestWithTestModel() {
        Set<Topic> topics = Set.of(topic1, topic2);

        topicService.saveAndLinkAll(topics, test);

        List<TestModel> tests = testRepo.findAllWithAuthorAndTopics();
        assertThat(tests).hasSize(1);
        TestModel testFound = tests.getFirst();

        assertThat(topicRepo.findAllByIdTestModel(testFound.getId()))
                .containsOnly(topic1, topic2)
                .flatExtracting(topic -> topic.getTestTopics().stream()
                        .map(TestTopic::getTest)
                        .collect(Collectors.toSet()))
                .containsOnly(testFound);
    }

    /**
     * <p>Тестирование базовой операции сохранения новой сущности {@code Topic}.
     * Проверяется, что после сохранения объекту присваивается ID.</p>
     * <br>
     * <b><ol>
     * <li>Подготовка:</li>
     * <ul>
     * <li>Создается новая сущность {@code Topic} без ID.</li>
     * </ul>
     * <br>
     * <li>Действия:</li>
     * <ul>
     * <li>Вызов метода {@code topicService.save(topic)}.</li>
     * </ul>
     * <br>
     * <li>Проверки:</li>
     * <ul>
     * <li>Проверить, что после сохранения объекту {@code Topic} был присвоен ненулевой ID.</li>
     * <li>Проверить, что тема с ожидаемым названием ("Save Topic") присутствует в общем списке тем в репозитории.</li>
     * </ul>
     * </ol></b>
     */
    @Test
    @DisplayName("Сохранение темы")
    void shouldSaveTopic() {
        Topic topic = createTopic("Save Topic");
        topic = topicService.save(topic);

        assertThat(topic.getId()).isNotNull();
        assertThat(topicRepo.findAll()).extracting(Topic::getTitle).contains("Save Topic");
    }

}
