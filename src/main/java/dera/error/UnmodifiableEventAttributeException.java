package dera.error;

public class UnmodifiableEventAttributeException extends Exception {
    public UnmodifiableEventAttributeException() {
    }

    public UnmodifiableEventAttributeException(String message) {
        super(message);
    }

    public UnmodifiableEventAttributeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnmodifiableEventAttributeException(Throwable cause) {
        super(cause);
    }

    public UnmodifiableEventAttributeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
