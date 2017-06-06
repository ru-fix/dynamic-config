package ru.fix.dynamic.config.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.util.ReflectionUtils;
import ru.fix.dynamic.config.api.DynamicPropertyMarshaller;
import ru.fix.dynamic.config.api.DynamicPropertySource;
import ru.fix.dynamic.config.spring.annotation.DynamicPropertyDescription;
import ru.fix.dynamic.config.spring.annotation.DynamicPropertySet;
import ru.fix.dynamic.config.spring.exception.DynamicPropertyInitializationException;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Ayrat Zulkarnyaev
 */
@Slf4j
public class PropertySetAwareBeanProcessor extends BaseZkConfigBeanProcessor {

    private final DynamicPropertyMarshaller propertyMarshaller;
    private LongAdder processingTime = new LongAdder();

    public PropertySetAwareBeanProcessor(DynamicPropertySource propertySource,
                                         DynamicPropertyMarshaller propertyMarshaller) {
        super(propertySource);
        this.propertyMarshaller = propertyMarshaller;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        final long startTime = System.currentTimeMillis();
        DynamicPropertySet annotation = bean.getClass().getAnnotation(DynamicPropertySet.class);
        if (annotation != null) {
            ReflectionUtils.doWithFields(bean.getClass(), field -> {

                field.setAccessible(true);
                DynamicPropertyDescription description = field.getAnnotation(DynamicPropertyDescription.class);
                if (description != null) {
                    processPropertyDescription(description, field.get(bean));
                }

            });

            ReflectionUtils.doWithMethods(bean.getClass(), method -> {
                method.setAccessible(true);
                DynamicPropertyDescription description = method.getAnnotation(DynamicPropertyDescription.class);
                if (description != null) {
                    try {
                        processPropertyDescription(description, method.invoke(bean));
                    } catch (InvocationTargetException e) {
                        throw new DynamicPropertyInitializationException(String.format("Failed to process dynamic " +
                                "property for method %s. Property id: %s", method.getName(), description.id()), e);
                    }
                }
            });
        }

        final long currentProcessingTime = System.currentTimeMillis() - startTime;
        processingTime.add(currentProcessingTime);
        log.debug("Resolving zk annotation for \"{}\" bean took {} ms. Sum of processing times is equal {} ms now.",
                beanName, currentProcessingTime, processingTime.sum());

        return bean;
    }

    private void processPropertyDescription(DynamicPropertyDescription description, Object defaultValue) {
        try {
            if (!getPropertySource().hasProperty(description.id())) {
                getPropertySource().putIfAbsent(description.id(), propertyMarshaller.marshall(defaultValue));
            }
            getPropertySource().upsertProperty(description.id() + "/_INFO", description.description());
            getPropertySource().upsertProperty(description.id() + "/_REBOOT_REQUIRED", String.valueOf(description.isFinal()));
        } catch (Exception e) {
            throw new DynamicPropertyInitializationException(
                    String.format("error property dynamic property '%s'", description.id()), e);
        }
    }

}
