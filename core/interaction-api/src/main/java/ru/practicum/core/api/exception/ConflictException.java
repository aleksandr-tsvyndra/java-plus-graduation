package ru.practicum.core.api.exception;

import java.io.Serial;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.function.Supplier;

public class ConflictException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = -7092856392954898635L;

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public ConflictException(Throwable cause) {
        super(cause);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public static Supplier<ConflictException> conflictException(String message, Object... args) {
        return () -> new ConflictException(message, args);
    }
}