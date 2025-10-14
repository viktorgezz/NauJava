package ru.viktorgezz.NauJava.util;

import ru.viktorgezz.NauJava.exception.BusinessException;
import ru.viktorgezz.NauJava.exception.Error;

import java.util.List;

/**
 * Утилиты для валидации и разбора аргументов команд.
 */
public final class CommandUtils {

    private CommandUtils() {
    }

    public static void validInput(List<String> inputs, int minSize) {
        if (inputs.size() <= minSize) {
            throw new BusinessException(
                    Error.INVALID_FIELD,
                    "Не хватает введенного поля"
            );
        }
    }

    public static Long getId(String id) {
        if (id == null || id.isEmpty()) {
            throw new BusinessException(
                    Error.INVALID_ID,
                    "id пустой или равен null"
            );
        }
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BusinessException(
                    Error.INVALID_ID,
                    "id не соответствует целочисленному формату"
            );
        }
    }

    public static String getStringField(String value, String title) {
        if (value == null || value.isEmpty()) {
            throw new BusinessException(
                    Error.INVALID_FIELD,
                    String.format("Поле %s - пустое или равен null", title)
            );
        }
        return value;
    }
}
