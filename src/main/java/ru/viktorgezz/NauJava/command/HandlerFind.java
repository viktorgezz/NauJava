package ru.viktorgezz.NauJava.command;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.viktorgezz.NauJava.service.intrf.TestService;

import java.util.List;

import static ru.viktorgezz.NauJava.util.CommandUtils.getId;
import static ru.viktorgezz.NauJava.util.CommandUtils.validInput;

@Component("find")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HandlerFind extends ACommandHandler implements CommandHandler {

    private List<String> args;

    public HandlerFind(TestService testService) {
        super(testService);
    }

    @Override
    public void exec() {
        validInput(args, 1);
        Long id = getId(args.get(1));
        System.out.printf("Найден тест: %s...%n", testService.findById(id));
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }
}
