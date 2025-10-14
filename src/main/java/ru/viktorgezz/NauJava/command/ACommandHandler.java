package ru.viktorgezz.NauJava.command;

import ru.viktorgezz.NauJava.service.intrf.TestService;

/**
 * Абстрактная базовая заготовка для обработчиков команд.
 *
 * <p>Содержит общий доступ к {@link ru.viktorgezz.NauJava.service.intrf.TestService}.
 * Конкретные обработчики реализуют интерфейс {@link CommandHandler}.</p>
 */
public abstract class ACommandHandler {

    protected final TestService testService;

    public ACommandHandler(TestService testService) {
        this.testService = testService;
    }
}
