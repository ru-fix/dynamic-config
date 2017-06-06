package ru.fix.dynamic.config.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import ru.fix.dynamic.config.api.DynamicPropertySource;

/**
 * @author Ayrat Zulkarnyaev
 */
public abstract class BaseZkConfigBeanProcessor implements BeanPostProcessor {

    private final DynamicPropertySource propertySource;

    public BaseZkConfigBeanProcessor(DynamicPropertySource propertySource) {
        this.propertySource = propertySource;
    }

    protected DynamicPropertySource getPropertySource() {
        return propertySource;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
