package com.otoomo.ioc.exception;

public class BeanRepeatRegisteredException extends RuntimeException {
    public BeanRepeatRegisteredException() {
    }

    public BeanRepeatRegisteredException(String message) {
        super(message);
    }

    public BeanRepeatRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanRepeatRegisteredException(Throwable cause) {
        super(cause);
    }

    public BeanRepeatRegisteredException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
