package ru.practicum.core.api.exception;

import java.io.Serial;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.function.Supplier;

public class DataAlreadyExistException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = -7092856392954898635L;

    public DataAlreadyExistException(String message) {
        super(message);
    }

    public DataAlreadyExistException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public DataAlreadyExistException(Throwable cause) {
        super(cause);
    }

    public DataAlreadyExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public static Supplier<DataAlreadyExistException> dataAlreadyExistException(String message, Object... args) {
        return () -> new DataAlreadyExistException(message, args);
    }
}