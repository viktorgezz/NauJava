package ru.viktorgezz.NauJava.util;

import org.springframework.beans.factory.ObjectFactory;
import ru.viktorgezz.NauJava.command.CommandHandler;

import java.util.HashMap;

public class CommandFactories extends HashMap<String, ObjectFactory<CommandHandler>> {
}
