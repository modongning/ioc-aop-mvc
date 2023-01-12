package com.otoomo.ioc.exception;

/**
 * bean创建异常
 */
public class BeanDefinitionNotFoundException extends RuntimeException {
    public BeanDefinitionNotFoundException() {
    }

    public BeanDefinitionNotFoundException(String message) {
        super(message);
    }

    public BeanDefinitionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanDefinitionNotFoundException(Throwable cause) {
        super(cause);
    }

    public BeanDefinitionNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
