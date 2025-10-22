package ru.viktorgezz.NauJava.util;

import ru.viktorgezz.NauJava.user.Role;
import ru.viktorgezz.NauJava.user.User;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

public class GeneratorRandomModel {


    public static User getRandomUser()
    {
        final String[] names = {"User1", "User2", "User3", "User4", "User5", "User6", "User7"};
        final String[] passwords = {"password1", "password2", "password3"};

        return new User(
                names[getRandomIndex(names.length)] + randomString(10),
                passwords[getRandomIndex(passwords.length)],
                Arrays.stream(Role.values()).findAny().orElseThrow()
        );
    }

    private static int getRandomIndex(int sizeArray) {
        return new Random().nextInt(sizeArray);
    }

    private static String randomString(int length) {
        final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        return new Random().ints(length, 0, CHARACTERS.length())
                .mapToObj(CHARACTERS::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }
}
