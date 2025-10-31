package ru.viktorgezz.NauJava.test;

import ru.viktorgezz.NauJava.test.dto.AuthorDto;
import ru.viktorgezz.NauJava.test.dto.TestResponseDto;

/**
 * Маппер для конвертации {@link TestModel} в DTO.
 */
public class TestMapper {

    /**
     * Конвертирует {@link TestModel} в {@link TestResponseDto}.
     *
     * @param testModel модель теста
     * @return DTO теста
     */
    public static TestResponseDto toDto(TestModel testModel) {
        if (testModel == null) {
            return null;
        }

        AuthorDto authorDto = null;
        if (testModel.getAuthor() != null) {
            authorDto = new AuthorDto(
                    testModel.getAuthor().getId(),
                    testModel.getAuthor().getUsername()
            );
        }

        return new TestResponseDto(
                testModel.getId(),
                testModel.getTitle(),
                testModel.getDescription(),
                testModel.getStatus(),
                authorDto
        );
    }
}

