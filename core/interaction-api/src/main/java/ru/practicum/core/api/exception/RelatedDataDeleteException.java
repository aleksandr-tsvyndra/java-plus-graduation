package ru.practicum.core.api.exception;

import java.io.Serial;
import java.io.Serializable;
import java.text.MessageFormat;

public class RelatedDataDeleteException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = -892134765987654321L;

    public RelatedDataDeleteException(String message) {
        super(message);
    }

    public RelatedDataDeleteException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public RelatedDataDeleteException(Throwable cause) {
        super(cause);
    }

    public RelatedDataDeleteException(String message, Throwable cause) {
        super(message, cause);
    }
}
