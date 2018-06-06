package dera.error;

public class NoSuchEventAttributeException extends Exception {
    public NoSuchEventAttributeException() {
    }

    public NoSuchEventAttributeException(String message) {
        super(message);
    }

    public NoSuchEventAttributeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchEventAttributeException(Throwable cause) {
        super(cause);
    }

    public NoSuchEventAttributeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
