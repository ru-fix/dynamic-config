package ru.fix.dynamic.config.spring;


import ru.fix.dynamic.config.spring.annotation.DynamicPropertyDescription;
import ru.fix.dynamic.config.spring.annotation.DynamicPropertySet;

/**
 * @author Ayrat Zulkarnyaev
 */
@DynamicPropertySet
public class TestPropertySet {

    public static final String SOME_PROPERTY_KEY = "key1";
    public static final String SOME_FIELD_PROPERTY_KEY = "key2";
    public static final String SOME_INT_FIELD_PROPERTY_KEY = "key3";

    @DynamicPropertyDescription(id = SOME_FIELD_PROPERTY_KEY,
            description = "The user city")
    String city = "Moscow";

    @DynamicPropertyDescription(id = SOME_INT_FIELD_PROPERTY_KEY,
            description = "The user country code")
    Integer countryPhoneCode = 7;

    @DynamicPropertyDescription(id = SOME_PROPERTY_KEY,
            description = "The default user")
    public User defaultUser() {
        return new User()
                .setName("Name")
                .setSecondName("Second name");
    }

}
