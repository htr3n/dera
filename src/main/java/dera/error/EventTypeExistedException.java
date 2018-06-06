package dera.error;

public class EventTypeExistedException extends Exception {
    public EventTypeExistedException() {
    }

    public EventTypeExistedException(String message) {
        super(message);
    }

    public EventTypeExistedException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventTypeExistedException(Throwable cause) {
        super(cause);
    }

    public EventTypeExistedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
