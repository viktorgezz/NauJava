package ru.viktorgezz.NauJava.command;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.viktorgezz.NauJava.model.TestModel;
import ru.viktorgezz.NauJava.service.intrf.TestService;

import java.util.List;

import static ru.viktorgezz.NauJava.util.CommandUtils.getId;
import static ru.viktorgezz.NauJava.util.CommandUtils.validInput;

/**
 * Реализация {@link CommandHandler}
 * <p>Обработчик для команды 'delete'. Удаляет существующий тест по его ID.</p>
 */
@Component("delete")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HandlerDelete extends ACommandHandler implements CommandHandler {


    public HandlerDelete(TestService testService) {
        super(testService);
    }

    @Override
    public void exec(List<String> args) {
        validInput(args, 1);
        Long id = getId(args.get(1));
        TestModel testModel = testService.findById(id);
        testService.deleteById(id);
        System.out.printf("Удален тест: %s...%n", testModel);
    }

}
