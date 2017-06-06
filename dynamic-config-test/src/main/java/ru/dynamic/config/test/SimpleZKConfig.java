package ru.dynamic.config.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.fix.dynamic.config.api.DynamicPropertyChangeListener;
import ru.fix.dynamic.config.api.DynamicPropertyMarshaller;
import ru.fix.dynamic.config.api.DynamicPropertySource;
import ru.fix.dynamic.config.api.JSonPropertyMarshaller;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by swarmshine on 19.10.2016.
 */
public class SimpleZKConfig implements DynamicPropertySource {

    private static final Logger logger = LoggerFactory.getLogger(SimpleZKConfig.class);

    private final Properties properties;

    private Map<String, Collection<DynamicPropertyChangeListener<?>>> listeners = new ConcurrentHashMap<>();

    private final DynamicPropertyMarshaller marshaller;

    public SimpleZKConfig(Properties properties) {
        this.properties = properties;
        this.marshaller = new JSonPropertyMarshaller();
    }

    @Override
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    @Override
    public <T> T getProperty(String key, Class<T> type) {
        return getProperty(key, type, null);
    }

    @Override
    public <T> T getProperty(String key, Class<T> type, T defaultValue) {
        String value = properties.get(key).toString();
        if (value == null) {
            return defaultValue;
        }
        return marshaller.unmarshall(value, type);
    }

    @Override
    public <T> void upsertProperty(String key, T propVal) throws Exception {
        properties.put(key, propVal);
        firePropertyChanged(key, propVal);
    }

    @Override
    public <T> void putIfAbsent(String key, T propVal) throws Exception {
        if (!properties.containsKey(key)) {
            properties.put(key, propVal);
        }
    }

    @Override
    public <T> void updateProperty(String key, T value) throws Exception {
        properties.put(key, value);
        firePropertyChanged(key, value);
    }

    @Override
    public <T> void addPropertyChangeListener(String propertyName, Class<T> type, DynamicPropertyChangeListener<T> typedListener) {
        addPropertyChangeListener(propertyName, value -> {
            T convertedValue = marshaller.unmarshall(value, type);
            typedListener.onPropertyChanged(convertedValue);
        });
    }

    private void addPropertyChangeListener(String propertyName, DynamicPropertyChangeListener<String> listener) {
        listeners.computeIfAbsent(propertyName, key -> new CopyOnWriteArrayList<>()).add(listener);
    }

    private <T> void firePropertyChanged(String propName, T value) {
        Collection<DynamicPropertyChangeListener<?>> zkPropertyChangeListeners = listeners.get(propName);
        if (zkPropertyChangeListeners != null) {
            zkPropertyChangeListeners.forEach(listener -> {
                try {
                    ((DynamicPropertyChangeListener<T>)listener).onPropertyChanged(value);
                } catch (Exception e) {
                    logger.error("Failed to update property {}", propName, e);
                }

            });
        }
    }

}
