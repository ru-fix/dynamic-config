package ru.fix.dynamic.config.api;

/**
 * @author Ayrat Zulkarnyaev
 */
public interface DynamicProperty<T> {

    DynamicProperty<T> addListener(DynamicPropertyChangeListener<T> listener);

    T get();

}
