package dera.error;

public class EventTypeNotExistedException extends Exception {
    public EventTypeNotExistedException() {
    }

    public EventTypeNotExistedException(String message) {
        super(message);
    }

    public EventTypeNotExistedException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventTypeNotExistedException(Throwable cause) {
        super(cause);
    }

    public EventTypeNotExistedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
