package dera.error;

public class InvalidEventAttributeException extends Exception {
    public InvalidEventAttributeException() {
    }

    public InvalidEventAttributeException(String message) {
        super(message);
    }

    public InvalidEventAttributeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidEventAttributeException(Throwable cause) {
        super(cause);
    }

    public InvalidEventAttributeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
