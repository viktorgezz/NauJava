package ru.viktorgezz.NauJava.command;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.viktorgezz.NauJava.model.TestModel;
import ru.viktorgezz.NauJava.service.intrf.TestService;

import java.util.List;

import static ru.viktorgezz.NauJava.util.CommandUtils.*;

/**
 * Реализация {@link CommandHandler}
 * <p>Обработчик для команды 'create'. Создает новый тест на основе переданных аргументов.</p>
 */
@Component("create")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HandlerCreate extends ACommandHandler implements CommandHandler {

    public HandlerCreate(TestService testService) {
        super(testService);
    }

    @Override
    public void exec(List<String> args) {
        validInput(args, 1);
        String title = getStringField(args.get(1), "title");
        String description = getStringField(args.get(2), "description");
        List<String> questions = args.subList(3, args.size());

        testService.createTest(new TestModel(title, description, questions));
        System.out.println("Тест успешно добавлен...");
    }
}
