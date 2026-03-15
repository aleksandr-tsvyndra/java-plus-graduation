package ru.practicum.core.api.exception;

import java.io.Serial;
import java.io.Serializable;
import java.text.MessageFormat;

public class StartAfterEndException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = -7092856392954898635L;

    public StartAfterEndException(String message) {
        super(message);
    }

    public StartAfterEndException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public StartAfterEndException(Throwable cause) {
        super(cause);
    }

    public StartAfterEndException(String message, Throwable cause) {
        super(message, cause);
    }
}
