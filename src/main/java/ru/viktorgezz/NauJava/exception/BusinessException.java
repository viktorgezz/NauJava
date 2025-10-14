package ru.viktorgezz.NauJava.exception;

/**
 * Исключение бизнес-уровня с сообщениями ошибок из {@link Error} и параметрами.
 */
public class BusinessException extends RuntimeException {

    private final Error error;
    private final Object[] args;

    public BusinessException(
            final Error error,
            final Object... args
    ) {
        super(getFormatterMessage(error, args));
        this.error = error;
        this.args = args;
    }

    private static String getFormatterMessage(Error error, Object[] args) {
        if (args == null || args.length == 0) {
            return error.getMessage();
        }
        return String.format(error.getMessage(), args);
    }

    public Error getError() {
        return error;
    }

    public Object[] getArgs() {
        return args;
    }
}
