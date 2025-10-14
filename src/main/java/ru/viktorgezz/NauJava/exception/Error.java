package ru.viktorgezz.NauJava.exception;

public enum Error {

    TEST_NOT_FOUND("Test с id: %s - не найден"),
    INVALID_ID("Неправильное id: %s"),
    INVALID_FIELD("Поле имеет ошибку: %s"),
    UNKNOWN_COMMAND("Неизвестная команда: %s. Введите 'help' для справки");

    private final String message;

    Error(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
