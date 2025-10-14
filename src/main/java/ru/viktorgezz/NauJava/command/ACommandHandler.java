package ru.viktorgezz.NauJava.command;

import ru.viktorgezz.NauJava.service.intrf.TestService;

public abstract class ACommandHandler {

    protected final TestService testService;

    public ACommandHandler(TestService testService) {
        this.testService = testService;
    }
}
