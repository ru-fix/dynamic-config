package ru.fix.dynamic.config.zk.spring;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Ayrat Zulkarnyaev
 */
@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(DynamicPropertyImportSelector.class)
public @interface EnableZkProperties {
}
