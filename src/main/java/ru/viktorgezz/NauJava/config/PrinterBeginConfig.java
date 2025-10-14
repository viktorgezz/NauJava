package ru.viktorgezz.NauJava.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для печати информационных данных приложения при старте.
 */
@Configuration
public class PrinterBeginConfig {

    @Value("${app.name}")
    private String name;

    @Value("${app.version}")
    private String version;

    public void printAppInfoOnStartup() {
        System.out.println("========================================");
        System.out.println("Сервис TestService успешно инициализирован.");

        System.out.printf("Имя приложения: %s%n", name);
        System.out.printf("Версия приложения: %s%n", version);

        System.out.println("========================================");
    }
}
