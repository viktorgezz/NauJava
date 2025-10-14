package ru.viktorgezz.NauJava.command;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.viktorgezz.NauJava.service.intrf.TestService;

@Component("findAll")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HandlerFindAll extends ACommandHandler implements CommandHandler{

    public HandlerFindAll(TestService testService) {
        super(testService);
    }

    @Override
    public void exec() {
        System.out.println(testService.findAll());
    }
}
