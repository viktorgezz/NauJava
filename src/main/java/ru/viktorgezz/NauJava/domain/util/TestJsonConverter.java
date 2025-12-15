package ru.viktorgezz.NauJava.domain.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import ru.viktorgezz.NauJava.domain.test.dto.TestUpdateContentDto;

public class TestJsonConverter {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .build();

    /**
     * Превращает JSON строку в объект TestUpdateContentDto.
     * Корректно маппит Enum Type и BigDecimal.
     *
     * @param jsonString входящая строка JSON
     * @return заполненный DTO
     * @throws IllegalArgumentException если JSON некорректен
     */
    public static TestUpdateContentDto parseDto(String jsonString) {
        try {
            return MAPPER.readValue(jsonString, TestUpdateContentDto.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Ошибка при чтении JSON для теста: " + e.getMessage(), e);
        }
    }
}
