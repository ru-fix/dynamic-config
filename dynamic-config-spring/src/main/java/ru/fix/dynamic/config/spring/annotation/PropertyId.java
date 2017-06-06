package ru.fix.dynamic.config.spring.annotation;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.*;

/**
 * @author Kamil Asfandiyarov
 */
@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PropertyId {

    /**
     * Property id
     */
    String value();

}
