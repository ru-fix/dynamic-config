package ru.fix.dynamic.config.spring.exception;

import org.springframework.beans.BeansException;

/**
 * @author Ayrat Zulkarnyaev
 */
public class DynamicPropertyInitializationException extends BeansException {

    public DynamicPropertyInitializationException(String message) {
        super(message);
    }

    public DynamicPropertyInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

}
