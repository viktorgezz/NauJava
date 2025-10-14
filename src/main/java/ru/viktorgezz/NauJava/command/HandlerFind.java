package ru.viktorgezz.NauJava.command;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.viktorgezz.NauJava.service.intrf.TestService;

import java.util.List;

import static ru.viktorgezz.NauJava.util.CommandUtils.getId;
import static ru.viktorgezz.NauJava.util.CommandUtils.validInput;

/**
 * Реализация {@link CommandHandler}
 * <p>Обработчик для команды 'find'. Находит и отображает тест по его ID.</p>
 */
@Component("find")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HandlerFind extends ACommandHandler implements CommandHandler {

    public HandlerFind(TestService testService) {
        super(testService);
    }

    @Override
    public void exec(List<String> args) {
        validInput(args, 1);
        Long id = getId(args.get(1));
        System.out.printf("Найден тест: %s...%n", testService.findById(id));
    }
}
