package de.rapha149.signgui.exception;

/**
 * An exception thrown when the server version is not supported by this api or an error occured during initialization.
 */
public class SignGUIVersionException extends Exception {

    /**
     * {@inheritDoc}
     */
    public SignGUIVersionException() {
    }

    /**
     * {@inheritDoc}
     */
    public SignGUIVersionException(String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     */
    public SignGUIVersionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * {@inheritDoc}
     */
    public SignGUIVersionException(Throwable cause) {
        super(cause);
    }

    /**
     * {@inheritDoc}
     */
    public SignGUIVersionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
