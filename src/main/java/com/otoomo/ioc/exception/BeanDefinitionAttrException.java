package com.otoomo.ioc.exception;

public class BeanDefinitionAttrException extends RuntimeException {
    public BeanDefinitionAttrException() {
    }

    public BeanDefinitionAttrException(String message) {
        super(message);
    }

    public BeanDefinitionAttrException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanDefinitionAttrException(Throwable cause) {
        super(cause);
    }

    public BeanDefinitionAttrException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
