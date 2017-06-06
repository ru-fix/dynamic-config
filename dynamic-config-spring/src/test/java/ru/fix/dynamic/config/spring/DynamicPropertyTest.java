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
import ru.fix.dynamic.config.api.DynamicPropertyHolder;
import ru.fix.dynamic.config.api.DynamicPropertyMarshaller;
import ru.fix.dynamic.config.api.DynamicPropertySource;
import ru.fix.dynamic.config.api.JSonPropertyMarshaller;
import ru.fix.dynamic.config.spring.annotation.DynamicProperty;
import ru.fix.dynamic.config.spring.config.DynamicPropertyConfig;

import java.util.Properties;

/**
 * @author Ayrat Zulkarnyaev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DynamicPropertyTest.Config.class)
public class DynamicPropertyTest {

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

    @DynamicProperty(TestPropertySet.SOME_PROPERTY_KEY)
    private DynamicPropertyHolder<User> defaultUser;

    @DynamicProperty(TestPropertySet.SOME_FIELD_PROPERTY_KEY)
    private DynamicPropertyHolder<String> defaultCity;

    @DynamicProperty(TestPropertySet.SOME_INT_FIELD_PROPERTY_KEY)
    private DynamicPropertyHolder<Integer> countryCode;

    @Test
    public void addDynamicPropertyListener() {

        System.out.println(defaultUser);
        System.out.println(defaultCity);
        System.out.println(countryCode);

        defaultUser.addListener(System.out::println);
    }

}
