package ru.viktorgezz.NauJava.util;

import ru.viktorgezz.NauJava.domain.topic.Topic;
import ru.viktorgezz.NauJava.domain.user.Role;
import ru.viktorgezz.NauJava.domain.user.User;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Генератор случайных данных моделей.
 */
public class GeneratorRandom {

    private static final String[] NAMES = {"User1", "User2", "User3", "User4", "User5", "User6", "User7"};
    private static final String[] PASSWORDS = {"password1", "password2", "password3"};
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String[] TOPICS = {"Topic1", "Topic2", "Topic3", "Topic4", "Topic5", "Topic6", "Topic7"};
    private static final String[] TEST_TITLE = {"Test1", "Test2", "Test3", "Test4", "Test5", "Test6", "Test7"};
    private static final String[] QUESTION_TEXT = {"Question1 text", "Question2 text", "Question3 text", "Question4 text", "Question5 text", "Question6 text", "Question7 text"};

    private GeneratorRandom() {
    }

    public static BigDecimal getRandomPoint() {
        return BigDecimal.valueOf(new Random().nextInt(10) + 1);
    }

    public static String getRandomQuestionText() {
        return QUESTION_TEXT[new Random().nextInt(QUESTION_TEXT.length)]  + " " + getRandomString(14);
    }

    public static String getRandomTestTitleRandom() {
        return TEST_TITLE[new Random().nextInt(TEST_TITLE.length)] + " " + getRandomString(14);
    }

    public static User getRandomUser() {
        return new User(
                NAMES[getRandomIndex(NAMES.length)] + getRandomString(10),
                PASSWORDS[getRandomIndex(PASSWORDS.length)],
                Arrays.stream(Role.values()).findAny().orElseThrow()
        );
    }

    public static Topic getRandomTopic() {
        return new Topic(TOPICS[getRandomIndex(TOPICS.length)] + " " + getRandomString(10));
    }

    private static int getRandomIndex(int sizeArray) {
        return new Random().nextInt(sizeArray);
    }

    public static String getRandomString(int length) {
        return new Random().ints(length, 0, CHARACTERS.length())
                .mapToObj(CHARACTERS::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    public static boolean getRandomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }
}
