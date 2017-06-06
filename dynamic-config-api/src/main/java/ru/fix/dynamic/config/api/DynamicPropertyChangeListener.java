package ru.fix.dynamic.config.api;

/**
 * @author Ayrat Zulkarnyaev
 */
@FunctionalInterface
public interface DynamicPropertyChangeListener<T> {

    void onPropertyChanged(T value);

}
