package io.github.rapha149.signgui;

public class SignGUIException extends RuntimeException {

    public SignGUIException() {
    }

    public SignGUIException(String message) {
        super(message);
    }

    public SignGUIException(String message, Throwable cause) {
        super(message, cause);
    }

    public SignGUIException(Throwable cause) {
        super(cause);
    }

    public SignGUIException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
