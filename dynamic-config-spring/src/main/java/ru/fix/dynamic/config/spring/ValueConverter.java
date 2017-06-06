package ru.fix.dynamic.config.spring;

import ru.fix.dynamic.config.api.DynamicPropertyMarshaller;

import java.math.BigDecimal;

class ValueConverter {

    private final DynamicPropertyMarshaller dynamicPropertyMarshaller;

    ValueConverter(DynamicPropertyMarshaller dynamicPropertyMarshaller) {
        this.dynamicPropertyMarshaller = dynamicPropertyMarshaller;
    }

    @SuppressWarnings("unchecked")
    <T> T convert(Class<T> type, String value, T defaultValue) {
        if (value == null) {
            return defaultValue;
        } else if (Byte.class.equals(type) || byte.class.equals(type)) {
            return (T) Byte.valueOf(value);
        } else if (Integer.class.equals(type) || int.class.equals(type)) {
            return (T) Integer.valueOf(value);
        } else if (Boolean.class.equals(type) || boolean.class.equals(type)) {
            return (T) Boolean.valueOf(value);
        } else if (Long.class.equals(type) || long.class.equals(type)) {
            return (T) Long.valueOf(value);
        } else if (Double.class.equals(type) || double.class.equals(type)) {
            return (T) Double.valueOf(value);
        } else if (BigDecimal.class.equals(type)) {
            return (T) new BigDecimal(value);
        } else if (String.class.equals(type)) {
            return (T) value;
        }
        return dynamicPropertyMarshaller.unmarshall(value, type);
    }

}
