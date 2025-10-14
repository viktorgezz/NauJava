package ru.viktorgezz.NauJava.command;

import java.util.List;

/**
 * Интерфейс обработчика конкретной команды.
 *
 * <p>Реализации принимают разобранные аргументы и выполняют
 * соответствующее действие.</p>
 */
public interface CommandHandler {

    /**
     * Выполняет команду с переданными аргументами.
     *
     * @param args список аргументов команды
     */
    void exec(List<String> args);
}
