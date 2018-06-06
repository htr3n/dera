package dera.error;

public class ActorExistedException extends Exception {
    public ActorExistedException() {
        super();
    }

    public ActorExistedException(String message) {
        super(message);
    }

    public ActorExistedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ActorExistedException(Throwable cause) {
        super(cause);
    }

    protected ActorExistedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
