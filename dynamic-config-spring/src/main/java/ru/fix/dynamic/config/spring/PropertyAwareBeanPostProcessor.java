package ru.fix.dynamic.config.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import ru.fix.dynamic.config.api.DynamicProperty;
import ru.fix.dynamic.config.api.DynamicPropertySource;
import ru.fix.dynamic.config.api.exception.PropertyNotFoundException;
import ru.fix.dynamic.config.spring.annotation.PropertyId;

import java.lang.reflect.ParameterizedType;

/**
 * @author Kamil Asfandiyarov
 */
@Slf4j
public class PropertyAwareBeanPostProcessor extends BaseZkConfigBeanProcessor {

    public PropertyAwareBeanPostProcessor(DynamicPropertySource dynamicPropertySource) {
        super(dynamicPropertySource);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), field -> {

            field.setAccessible(true);

            PropertyId zkConfigAnnotation = field.getAnnotation(PropertyId.class);
            if (zkConfigAnnotation != null) {

                Class<?> fieldType = field.getType();

                if (!fieldType.isAssignableFrom(DynamicProperty.class)) {
                    log.warn("Dynamic property annotation is applicable only on fields of DynamicProperty type, not '{}'," +
                            " bean name - '{}'.", fieldType, beanName);
                    return;
                }

                String propertyId = zkConfigAnnotation.value();
                if (StringUtils.isEmpty(propertyId)) {
                    throw new PropertyNotFoundException(
                            String.format("Empty zk property id for bean %s, field %s", beanName, field.getName()));
                }

                if (!getPropertySource().hasProperty(propertyId)) {
                    throw new PropertyNotFoundException(
                            String.format("Can't find property '%s'. Processed class %s, field %s", propertyId, beanName,
                                    field.getName())
                    );
                }
                ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                Class propertyClass = (Class) parameterizedType.getActualTypeArguments()[0];

                field.set(bean, new DynamicProperty<>(getPropertySource(), propertyId, propertyClass));
            }

        });
        return bean;
    }


}
