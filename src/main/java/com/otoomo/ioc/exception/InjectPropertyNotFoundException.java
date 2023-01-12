package com.otoomo.ioc.exception;

public class InjectPropertyNotFoundException extends RuntimeException {

    public InjectPropertyNotFoundException() {
    }

    public InjectPropertyNotFoundException(String message) {
        super(message);
    }

    public InjectPropertyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public InjectPropertyNotFoundException(Throwable cause) {
        super(cause);
    }

    public InjectPropertyNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
