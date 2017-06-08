package ru.fix.dynamic.config.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.dynamic.config.test.SimpleZKConfig;
import ru.fix.dynamic.config.api.*;
import ru.fix.dynamic.config.spring.annotation.PropertyId;
import ru.fix.dynamic.config.spring.config.DynamicPropertyConfig;

import java.util.Properties;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Ayrat Zulkarnyaev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PropertyIdTest.Config.class)
public class PropertyIdTest {

    @Configuration
    @Import(DynamicPropertyConfig.class)
    @ComponentScan("ru.fix.dynamic.config.spring")
    public static class Config {

        @Bean
        public DynamicPropertySource dynamicPropertySource() {
            Properties properties = new Properties();
            return new SimpleZKConfig(properties);
        }

        @Bean
        public DynamicPropertyMarshaller propertyMarshaller() {
            return new JSonPropertyMarshaller();
        }

    }

    @PropertyId(TestPropertySet.SOME_PROPERTY_KEY)
    private DynamicProperty<User> defaultUser;

    @PropertyId(TestPropertySet.SOME_FIELD_PROPERTY_KEY)
    private DynamicProperty<String> defaultCity;

    @PropertyId(TestPropertySet.SOME_INT_FIELD_PROPERTY_KEY)
    private DynamicProperty<Integer> countryCode;

    @Test
    public void addDynamicPropertyListener() {
        assertThat(defaultUser, notNullValue());
        assertThat(defaultCity, notNullValue());
        assertThat(countryCode, notNullValue());
    }

}
