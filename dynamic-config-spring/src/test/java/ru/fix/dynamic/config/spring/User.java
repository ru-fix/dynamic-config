package ru.fix.dynamic.config.spring;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Ayrat Zulkarnyaev
 */
@Data
@Accessors(chain = true)
public class User {

    private String name;
    private String secondName;

}
