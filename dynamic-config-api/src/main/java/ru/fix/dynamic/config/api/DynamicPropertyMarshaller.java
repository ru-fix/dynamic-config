package ru.fix.dynamic.config.api;

import ru.fix.dynamic.config.api.exception.DynamicPropertyDeserializationException;
import ru.fix.dynamic.config.api.exception.DynamicPropertySerializationException;

/**
 * @author Ayrat Zulkarnyaev
 */
public interface DynamicPropertyMarshaller {

    String marshall(Object marshalledObject) throws DynamicPropertySerializationException;

    <T> T unmarshall(String rawString, Class<T> clazz) throws DynamicPropertyDeserializationException;

}
