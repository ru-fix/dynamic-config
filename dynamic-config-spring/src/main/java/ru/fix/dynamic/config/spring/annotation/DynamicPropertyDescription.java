package ru.fix.dynamic.config.spring.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Ayrat Zulkarnyaev
 */
@Target(value = {ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicPropertyDescription {

    /**
     * ZK node name
     */
    String id();

    /**
     * Description of the property.
     * Plain text or 'markdown' markup form accepted
     * <p>
     * Will be stored in /_INFO subnode of the property.
     */
    String description();

    boolean isFinal() default false;

}
