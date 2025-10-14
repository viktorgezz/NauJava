package ru.viktorgezz.NauJava.config;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.viktorgezz.NauJava.command.CommandHandler;
import ru.viktorgezz.NauJava.exception.BusinessException;
import ru.viktorgezz.NauJava.runner.CommandProcessor;
import ru.viktorgezz.NauJava.util.CommandFactories;
import ru.viktorgezz.NauJava.util.TestArrayList;

import java.util.Scanner;

/**
 * Основной конфигурационный класс приложения.
 * <p>
 * Отвечает за создание и настройку ключевых бинов, необходимых для работы
 * консольного интерфейса (CLI) и основной бизнес-логики.
 * Здесь регистрируются обработчик команд, репозиторий в памяти и главный цикл приложения.
 * </p>
 */
@Configuration
public class ConfigCommon {

    @Bean
    public CommandLineRunner commandScanner(CommandProcessor commandProcessor) {
        return args ->
        {
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.println("Введите команду. 'exit' для выхода, или 'help' для справки ");
                while (true) {
                    try {
                        System.out.print("> ");
                        String input = scanner.nextLine();
                        if ("exit".equalsIgnoreCase(input.trim())) {
                            System.out.println("Выход из программы...");
                            break;
                        }
                        commandProcessor.processCommand(input);
                    } catch (BusinessException e) {
                        System.err.printf("%n %s %n", e.getMessage());
                        System.out.println();
                    }
                }
            }
        };
    }

    @Bean
    public TestArrayList testArrayList() {
        return new TestArrayList();
    }

    @Bean
    public CommandFactories commandFactories(
            ApplicationContext context
    ) {
        CommandFactories map = new CommandFactories();
        String[] commandHandlerNames = context.getBeanNamesForType(CommandHandler.class);

        for (String beanName : commandHandlerNames) {
            ObjectFactory<CommandHandler> factory = () -> (CommandHandler) context.getBean(beanName);
            map.put(beanName, factory);
        }

        return map;
    }
}
