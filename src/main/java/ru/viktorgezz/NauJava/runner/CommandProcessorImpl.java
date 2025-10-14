package ru.viktorgezz.NauJava.runner;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.viktorgezz.NauJava.command.CommandHandler;
import ru.viktorgezz.NauJava.exception.BusinessException;
import ru.viktorgezz.NauJava.exception.Error;
import ru.viktorgezz.NauJava.util.CommandFactories;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Реализация {@link CommandProcessor}
 */
@Component
public class CommandProcessorImpl implements CommandProcessor {

    private final CommandFactories commandFactories;

    @Autowired
    public CommandProcessorImpl(CommandFactories commandFactories) {
        this.commandFactories = commandFactories;
    }

    @Override
    public void processCommand(String input) {
        List<String> cmd = parseInput(input);
        if (cmd.isEmpty()) {
            return;
        }

        ObjectFactory<CommandHandler> handlerFactory = Optional
                .ofNullable(commandFactories.get(cmd.getFirst()))
                .orElseThrow(() ->
                        new BusinessException(
                                Error.UNKNOWN_COMMAND,
                                cmd.getFirst()));

        CommandHandler handler = handlerFactory.getObject();
        handler.exec(cmd);
    }

    private List<String> parseInput(String input) {
        Pattern pattern = Pattern.compile("\"[^\"]*\"|\\S+"); // находит либо "фразы в кавычках", либо отдельные слова.
        Matcher matcher = pattern.matcher(input);
        List<String> parts = new ArrayList<>();
        while (matcher.find()) {
            String part = matcher.group();
            if (part.startsWith("\"") && part.endsWith("\"")) {
                parts.add(part.substring(1, part.length() - 1));
            } else {
                parts.add(part);
            }
        }
        return parts;
    }
}
