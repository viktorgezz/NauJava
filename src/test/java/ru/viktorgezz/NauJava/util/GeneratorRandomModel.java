package ru.viktorgezz.NauJava.util;

import ru.viktorgezz.NauJava.domain.user.Role;
import ru.viktorgezz.NauJava.domain.user.User;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Генератор случайных данных моделей.
 */
public class GeneratorRandomModel {

    private static final String[] NAMES = {"User1", "User2", "User3", "User4", "User5", "User6", "User7"};
    private static final String[] PASSWORDS = {"password1", "password2", "password3"};
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private GeneratorRandomModel() {
    }

    public static User getRandomUser() {
        return new User(
                NAMES[getRandomIndex(NAMES.length)] + randomString(10),
                PASSWORDS[getRandomIndex(PASSWORDS.length)],
                Arrays.stream(Role.values()).findAny().orElseThrow()
        );
    }

    private static int getRandomIndex(int sizeArray) {
        return new Random().nextInt(sizeArray);
    }

    private static String randomString(int length) {
        return new Random().ints(length, 0, CHARACTERS.length())
                .mapToObj(CHARACTERS::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }
}
