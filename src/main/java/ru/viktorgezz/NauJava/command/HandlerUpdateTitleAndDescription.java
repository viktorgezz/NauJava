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
 * <p>Обработчик для команды 'updateTitleAndDescription'. Обновляет название и описание теста по ID.</p>
 */
@Component("updateTitleAndDescription")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HandlerUpdateTitleAndDescription extends ACommandHandler implements CommandHandler {

    public HandlerUpdateTitleAndDescription(TestService testService) {
        super(testService);
    }

    @Override
    public void exec(List<String> args) {
        validInput(args, 3);
        Long id = getId(args.get(1));
        String title = getStringField(args.get(2), "title");
        String description = getStringField(args.get(3), "description");
        TestModel testOld = testService.findById(id);

        testService.updateTitleAndDescription(id, title, description);
        System.out.printf(
                "Успешно обновлено: %s.%nБыло: %s%n",
                testService.findById(id),
                testOld
        );
    }
}
