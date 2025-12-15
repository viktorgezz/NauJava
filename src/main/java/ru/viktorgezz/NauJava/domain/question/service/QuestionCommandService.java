package ru.viktorgezz.NauJava.domain.question.service;

import ru.viktorgezz.NauJava.domain.question.Question;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.test.dto.TestUpdateContentDto;

/**
 * Контракт командного сервиса для вопросов {@link Question} в тестах {@link TestModel}.
 */
public interface QuestionCommandService {

    /**
     * Создает новый вопрос и связывает его с тестом без сохранения в БД.
     *
     * @param questionDto DTO с данными вопроса.
     * @param test тест для связи с вопросом.
     * @return созданный вопрос.
     */
    Question createQuestionWithoutSave(
            TestUpdateContentDto.QuestionDto questionDto,
            TestModel test
    );

    /**
     * Обновляет существующий вопрос без сохранения в БД.
     *
     * @param question существующий вопрос для обновления.
     * @param questionDto DTO с новыми данными вопроса.
     */
    void updateQuestionWithoutSave(
            Question question,
            TestUpdateContentDto.QuestionDto questionDto
    );

    /**
     * Сохраняет коллекцию вопросов в БД.
     *
     * @param questions коллекция вопросов для сохранения.
     */
    void saveAll(Iterable<Question> questions);
}
