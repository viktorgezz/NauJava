package ru.viktorgezz.NauJava.util;

import java.util.concurrent.atomic.AtomicLong;

public final class IdGenerator {

    private static final AtomicLong counter = new AtomicLong(1);

    private IdGenerator() {
    }

    public static long getNextId() {
        return counter.getAndIncrement();
    }
}
