package ru.viktorgezz.NauJava.runner;

/**
 * Интерфейс процессора команд пользовательского ввода.
 *
 * <p>Инкапсулирует разбор введённой строки и делегирование
 * выполнения соответствующему обработчику команды.</p>
 */
public interface CommandProcessor {

    void processCommand(String input);
}
