package ru.viktorgezz.testing_system.domain.test;

import ru.viktorgezz.testing_system.domain.test.dto.TestMetadataResponseDto;
import ru.viktorgezz.testing_system.domain.topic.Topic;

import java.util.stream.Collectors;

/**
 * Маппер для конвертации {@link TestModel} в DTO.
 */
public class TestMapper {

    /**
     * Конвертирует {@link TestModel} в {@link TestMetadataResponseDto}.
     *
     * @param testModel модель теста
     * @return DTO теста
     */
    public static TestMetadataResponseDto toDto(TestModel testModel) {
        if (testModel == null) {
            return null;
        }

        TestMetadataResponseDto.AuthorDto authorDto = null;
        if (testModel.getAuthor() != null) {
            authorDto = new TestMetadataResponseDto.AuthorDto(
                    testModel.getAuthor().getId(),
                    testModel.getAuthor().getUsername()
            );
        }

        return new TestMetadataResponseDto(
                testModel.getId(),
                testModel.getTitle(),
                testModel.getDescription(),
                testModel.getStatus(),
                authorDto,
                testModel.getTopics()
                        .stream()
                        .map(Topic::getTitle)
                        .collect(Collectors.toSet())
        );
    }
}

