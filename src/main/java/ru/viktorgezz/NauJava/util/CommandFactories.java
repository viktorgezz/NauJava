package ru.viktorgezz.NauJava.util;

import org.springframework.beans.factory.ObjectFactory;
import ru.viktorgezz.NauJava.command.CommandHandler;

import java.util.HashMap;

/**
 * Реестр фабрик обработчиков команд по имени команды.
 */
public class CommandFactories extends HashMap<String, ObjectFactory<CommandHandler>> {
}
