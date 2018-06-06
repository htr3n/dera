package dera.error;

public class ActorNotExistedException extends Exception {
    public ActorNotExistedException() {
        super();
    }

    public ActorNotExistedException(String message) {
        super(message);
    }

    public ActorNotExistedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ActorNotExistedException(Throwable cause) {
        super(cause);
    }

    protected ActorNotExistedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
