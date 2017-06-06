package ru.fix.dynamic.config.api;

import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Contain property initial value. Automatically register property change listener.
 */
public class DynamicProperty<T> {

    private static final Logger log = getLogger(DynamicProperty.class);
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    private final DynamicPropertySource propertySource;
    private final Class<T> type;
    private final String name;
    private final T defaultValue;
    private volatile T currentValue;

    private List<DynamicPropertyChangeListener<T>> listeners = new CopyOnWriteArrayList<>();

    public DynamicProperty(DynamicPropertySource propertySource, String name, Class<T> type) {
        this(propertySource, name, type, null);
    }

    public DynamicProperty(DynamicPropertySource propertySource, String name, Class<T> type, T defaultValue) {
        this.propertySource = propertySource;
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;

        init();
    }

    private void init() {
        propertySource.addPropertyChangeListener(
                name,
                type,
                newValue -> listeners.forEach(listener -> executor.submit(() -> {
                    try {
                        listener.onPropertyChanged(newValue);
                    } catch (Exception e) {
                        log.error("Failed to update property {} with value", name, newValue, e);
                    }
                }))
        );
        currentValue = propertySource.getProperty(name, type, defaultValue);
    }

    public T get() {
        return currentValue;
    }

    public DynamicProperty<T> addListener(DynamicPropertyChangeListener<T> listener) {
        listeners.add(listener);
        return this;
    }


    @Override
    public String toString() {
        return "DynamicProperty{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", defaultValue=" + defaultValue +
                ", currentValue=" + currentValue +
                '}';
    }

}
