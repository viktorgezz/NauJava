package ru.viktorgezz.NauJava.domain.test.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.viktorgezz.NauJava.domain.test.Status;

import java.util.List;

/**
 * DTO метаданных теста: идентификатор, заголовок, описание, статус и список тем.
 */
public record TestMetadataRequestDto(
        Long idTest,
        @NotBlank(message = "Название теста не может быть пустым") @Size(min = 1, max = 100, message = "Title должен быть от 1 до 100 символов")
        String title,
        @Size(max = 325, message = "Описание не может превышать 325 символов")
        String description,
        @NotNull
        Status status,
        @NotNull(message = "Список тем не может быть null (может быть пустым)") @Size(max = 5, message = "Количество не может быть более 5")
        List<String> titlesTopic
) {
}
