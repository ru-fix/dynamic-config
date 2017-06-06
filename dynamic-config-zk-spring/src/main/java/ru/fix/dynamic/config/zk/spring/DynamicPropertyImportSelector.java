package ru.fix.dynamic.config.zk.spring;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import ru.fix.dynamic.config.spring.config.DynamicPropertyConfig;

/**
 * @author Ayrat Zulkarnyaev
 */
public class DynamicPropertyImportSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{DynamicPropertyConfig.class.getName(), ZkPropertySourceConfig.class.getName()};
    }

}
