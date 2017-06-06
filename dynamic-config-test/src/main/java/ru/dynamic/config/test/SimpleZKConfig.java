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

    private Map<String, Collection<DynamicPropertyChangeListener<String>>> listeners = new ConcurrentHashMap<>();

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
    public String getProperty(String key) {
        return properties.get(key).toString();
    }

    @Override
    public String getProperty(String key, String defaulValue) {
        return properties.get(key) == null ? defaulValue : properties.get(key).toString();
    }

    @Override
    public <T> T getProperty(String key, Class<T> type) {
        return getProperty(key, type, null);
    }

    @Override
    public <T> T getProperty(String key, Class<T> type, T defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return marshaller.unmarshall(value, type);
    }

    @Override
    public Properties getAllProperties() throws Exception {
        return properties;
    }

   @Override
    public Properties uploadInitialProperties(String propertiesPath) throws Exception {
        properties.load(getClass().getResourceAsStream(propertiesPath));
        return properties;
    }

    @Override
    public void upsertProperty(String key, String propVal) throws Exception {
        properties.put(key, propVal);
        firePropertyChanged(key, propVal);
    }

    @Override
    public void putIfAbsent(String key, String propVal) throws Exception {
        if (!properties.containsKey(key)) {
            properties.put(key, propVal);
        }
    }

    @Override
    public void updateProperty(String key, String value) throws Exception {
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

    @Override
    public void addPropertyChangeListener(String propertyName, DynamicPropertyChangeListener<String> listener) {
        listeners.computeIfAbsent(propertyName, key -> new CopyOnWriteArrayList<>()).add(listener);
    }

    private void firePropertyChanged(String propName, String value) {
        Collection<DynamicPropertyChangeListener<String>> zkPropertyChangeListeners = listeners.get(propName);
        if (zkPropertyChangeListeners != null) {
            zkPropertyChangeListeners.forEach(listener -> {
                try {
                    listener.onPropertyChanged(value);
                } catch (Exception e) {
                    logger.error("Failed to update property {}", propName, e);
                }

            });
        }
    }

}
