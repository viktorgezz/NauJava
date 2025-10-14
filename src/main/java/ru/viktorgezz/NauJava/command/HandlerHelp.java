package ru.viktorgezz.NauJava.command;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.viktorgezz.NauJava.service.intrf.TestService;

import java.util.List;

/**
 * Реализация {@link CommandHandler}
 * <p>Обработчик для команды 'help'. Выводит в консоль справочную информацию по всем командам.</p>
 */
@Component("help")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HandlerHelp extends ACommandHandler implements CommandHandler {

    public HandlerHelp(TestService testService) {
        super(testService);
    }

    @Override
    public void exec(List<String> args) {
        System.out.println("""
        
        СПРАВКА ПО КОМАНДАМ
        --------------------------------------------------------------------------------------------------
        ПРИМЕЧАНИЕ: Аргументы, содержащие пробелы, необходимо заключать в двойные кавычки "".
        
        1. Создать новый тест:
           Формат: create "<название>" "<описание>" "<вопрос1>" "<вопрос2>" ...
        
        2. Найти тест по ID:
           Формат: find <id>
        
        3. Показать все тесты:
           Формат: findAll
        
        4. Удалить тест по ID:
           Формат: delete <id>
        
        5. Обновить название и описание теста:
           Формат: updateTitleAndDescription <id> "<новое_название>" "<новое_описание>"
        
        6. Показать эту справку:
           Формат: help
        
        7. Выйти из программы:
           Формат: exit
        --------------------------------------------------------------------------------------------------
        """);
    }
}
