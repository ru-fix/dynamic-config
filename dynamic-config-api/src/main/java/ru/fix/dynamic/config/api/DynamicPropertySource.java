package ru.fix.dynamic.config.api;

import java.util.Properties;

/**
 * @author Ayrat Zulkarnyaev
 */
public interface DynamicPropertySource {

    /**
     * Return {@code true} if property exists, {@code false} otherwise
     */
    boolean hasProperty(String key);

    /**
     * Returns property value for specified key in required type. Currently
     * supported only {@link String},{@link Integer},{@link Long},
     * {@link Boolean} types.
     * <p>
     * There are no guarantees of accuracy. This is merely the most recent view
     * of the data.
     * </p>
     *
     * @param key  property name
     * @param type property type
     * @return property value, converted to the required type or {@code null} if
     * there is no such property
     * @throws IllegalArgumentException if conversation are not supported
     * @throws NumberFormatException    if property value are not numeric and {@link Integer} or
     *                                  {@link Long} type are specified
     */
    <T> T getProperty(String key, Class<T> type);

    <T> T getProperty(String key, Class<T> type, T defaultValue);

    /**
     * Updates (if already exists) or inserts property.
     *
     * @param key     key
     * @param propVal value
     * @throws Exception
     * @see #updateProperty(String, Object)
     */
    <T> void upsertProperty(String key, T propVal) throws Exception;

    /**
     * Set propertyValue if absent (if node isn't present)
     * <p>
     * If node presents (including empty value), node's value remains unchanged
     *
     * @param key     key
     * @param propVal value
     */
    <T> void putIfAbsent(String key, T propVal) throws Exception;

    /**
     * Updates property value
     *
     * @param key   key name
     * @param value new value
     * @throws Exception
     * @see #upsertProperty(String, Object)
     */
    <T> void updateProperty(String key, T value) throws Exception;

    /**
     * Registers property change listener. Listener will trigger for
     * add/update/remove actions on specified property.
     *
     * @param propertyName property name
     * @param listener     listener
     */
    <T> void addPropertyChangeListener(String propertyName, Class<T> type, DynamicPropertyChangeListener<T> listener);

}
